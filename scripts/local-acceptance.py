#!/usr/bin/env python3
"""Build and run the marketplace E2E flow in a disposable Compose project."""

import argparse
import os
import secrets
import shutil
import socket
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.request
import uuid
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
COMPOSE_FILE = ROOT / "docker-compose.yml"


def free_ports(count):
    listeners = [socket.socket(socket.AF_INET, socket.SOCK_STREAM) for _ in range(count)]
    try:
        for listener in listeners:
            listener.bind(("127.0.0.1", 0))
        return [listener.getsockname()[1] for listener in listeners]
    finally:
        for listener in listeners:
            listener.close()


def write_environment(path):
    nacos_port, gateway_port = free_ports(2)
    values = {
        "MYSQL_ROOT_PASSWORD": secrets.token_hex(24),
        "MYSQL_PASSWORD": secrets.token_hex(24),
        "NEO4J_PASSWORD": secrets.token_hex(24),
        "NACOS_AUTH_ENABLED": "false",
        "NACOS_USERNAME": "nacos",
        "NACOS_PASSWORD": "nacos",
        "NACOS_NAMESPACE": "public",
        "NACOS_GROUP": "MICRO_SERVER",
        "JWT_SECRET": secrets.token_hex(32),
        "INTERNAL_SERVICE_TOKEN": secrets.token_hex(32),
        "PAYMENT_MOCK_CALLBACK_TOKEN": secrets.token_hex(32),
        "PAYMENT_CHANNEL": "MOCK_WECHAT",
        "NACOS_HOST_PORT": str(nacos_port),
        "GATEWAY_HOST_PORT": str(gateway_port),
    }
    path.write_text("".join(key + "=" + value + "\n" for key, value in values.items()), encoding="utf-8")
    path.chmod(0o600)
    return values


def run_command(command, env=None, quiet=False):
    result = subprocess.run(
        command, cwd=ROOT, env=env, check=False,
        stdout=subprocess.PIPE if quiet else None,
        stderr=subprocess.STDOUT if quiet else None,
        text=True,
    )
    if result.returncode:
        if quiet and result.stdout:
            print(result.stdout, file=sys.stderr)
        raise RuntimeError("command failed: " + " ".join(command[:4]))
    return result.stdout if quiet else ""


def wait_url(name, url, timeout, expected_payload=None):
    deadline = time.monotonic() + timeout
    last_error = "not reached"
    while time.monotonic() < deadline:
        try:
            with urllib.request.urlopen(url, timeout=5) as response:
                body = response.read().decode("utf-8", errors="replace")
                if response.status < 500 and (expected_payload is None or expected_payload in body):
                    print("Ready: " + name)
                    return
                last_error = "HTTP " + str(response.status)
        except (urllib.error.URLError, TimeoutError) as error:
            last_error = str(error)
        time.sleep(2)
    raise RuntimeError("timed out waiting for " + name + " (" + last_error + ")")


def wait_container_health(compose, service, timeout):
    deadline = time.monotonic() + timeout
    last_state = "not created"
    while time.monotonic() < deadline:
        container_id = run_command(compose + ["ps", "-q", service], quiet=True).strip()
        if container_id:
            last_state = run_command(
                ["docker", "inspect", "--format", "{{.State.Health.Status}}", container_id],
                quiet=True,
            ).strip()
            if last_state == "healthy":
                print("Ready: " + service)
                return
        time.sleep(2)
    raise RuntimeError("timed out waiting for " + service + " health (" + last_state + ")")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--skip-build", action="store_true", help="reuse already-built Maven service jars")
    parser.add_argument("--keep", action="store_true", help="keep the isolated Compose project and its temporary secrets for debugging")
    args = parser.parse_args()

    if shutil.which("docker") is None:
        raise RuntimeError("Docker CLI is required; install Docker Desktop or Docker Engine with Compose v2")
    run_command(["docker", "compose", "version"], quiet=True)
    project = "micro-server-own-e2e-" + uuid.uuid4().hex[:12]
    temp_dir = Path(tempfile.mkdtemp(prefix="micro-server-own-e2e-"))
    env_file = temp_dir / "e2e.env"
    values = write_environment(env_file)
    compose = [
        "docker", "compose", "--project-name", project,
        "--env-file", str(env_file), "-f", str(COMPOSE_FILE),
    ]
    process_env = os.environ.copy()
    process_env.update(values)
    process_env.update({
        "NACOS_SERVER": "http://127.0.0.1:" + values["NACOS_HOST_PORT"],
        "NACOS_AUTH_ENABLED": "false",
        "NACOS_USERNAME": values["NACOS_USERNAME"],
        "NACOS_PASSWORD": values["NACOS_PASSWORD"],
        "NACOS_NAMESPACE": values["NACOS_NAMESPACE"],
        "NACOS_GROUP": values["NACOS_GROUP"],
    })
    success = False
    try:
        if not args.skip_build:
            print("Building Java service jars...")
            run_command([str(ROOT / "mvnw"), "-B", "-ntp", "-DskipTests", "package"])

        print("Starting isolated database and Nacos containers...")
        run_command(compose + ["up", "-d", "mysql", "neo4j", "nacos"], env=process_env)
        wait_container_health(compose, "mysql", 180)
        wait_container_health(compose, "neo4j", 180)
        wait_url("Nacos", process_env["NACOS_SERVER"] + "/nacos/", 180)

        print("Importing local-only Nacos configuration...")
        run_command(["bash", str(ROOT / "scripts/import-nacos-config.sh")], env=process_env)

        print("Building and starting application services...")
        run_command(compose + ["--profile", "app", "up", "-d", "--build"], env=process_env)
        gateway_url = "http://127.0.0.1:" + values["GATEWAY_HOST_PORT"]
        wait_url("API Gateway", gateway_url + "/actuator/health", 240, '"status":"UP"')

        print("Running synthetic marketplace end-to-end flow...")
        e2e_env = process_env.copy()
        e2e_env.update({
            "GATEWAY_URL": gateway_url,
            "E2E_COMPOSE_PROJECT": project,
            "E2E_COMPOSE_ENV_FILE": str(env_file),
            "E2E_COMPOSE_FILE": str(COMPOSE_FILE),
        })
        run_command([sys.executable, str(ROOT / "scripts/marketplace-e2e.py")], env=e2e_env)
        success = True
    finally:
        if args.keep:
            print("Kept disposable project: " + project)
            print("Temporary credentials: " + str(env_file))
            print("Stop and remove it with: " + " ".join(compose + ["down", "--volumes", "--remove-orphans"]))
        else:
            print("Removing isolated containers, network, and named volumes...")
            result = subprocess.run(
                compose + ["down", "--volumes", "--remove-orphans"], cwd=ROOT,
                env=process_env, check=False,
            )
            if result.returncode:
                print("Cleanup failed; remove only Compose project " + project, file=sys.stderr)
                success = False
            if not args.keep:
                shutil.rmtree(temp_dir, ignore_errors=True)
    if not success:
        return 1
    if args.keep:
        print("Disposable local acceptance passed; the isolated project was retained.")
    else:
        print("Disposable local acceptance completed and cleaned up.")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (RuntimeError, OSError) as error:
        print("Local acceptance failed: " + str(error), file=sys.stderr)
        sys.exit(1)
