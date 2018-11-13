package com.own.owncommon.core;


import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class RestTemplateExt {
	
	private RestTemplate restTemplate;

	public RestTemplateExt (RestTemplate restTemplate){
		this.restTemplate = restTemplate;
	}
	
	/**
	 * 有返回值的PUT方法
	 * @param url
	 * @param request
	 * @param responseType
	 * @return
	 * @throws RestClientException
	 */
	public <T> T put(String url, Object request, Class<T> responseType) throws RestClientException {
		RequestCallback requestCallback = new HttpEntityRequestCallback(request,responseType);
		HttpMessageConverterExtractor<T> responseExtractor = new HttpMessageConverterExtractor<T>(responseType, restTemplate.getMessageConverters());
		return restTemplate.execute(url, HttpMethod.PUT, requestCallback, responseExtractor);
	}
	
	/**
	 * Request callback implementation that writes the given object to the request stream.
	 */
	private class HttpEntityRequestCallback extends AcceptHeaderRequestCallback {
		private final HttpEntity<?> requestEntity;
		private HttpEntityRequestCallback(Object requestBody) {
			this(requestBody, null);
		}
		private HttpEntityRequestCallback(Object requestBody, Type responseType) {
			super(responseType);
			if (requestBody instanceof HttpEntity) {
				this.requestEntity = (HttpEntity<?>) requestBody;
			}
			else if (requestBody != null) {
				this.requestEntity = new HttpEntity<Object>(requestBody);
			}
			else {
				this.requestEntity = HttpEntity.EMPTY;
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public void doWithRequest(ClientHttpRequest httpRequest) throws IOException {
			super.doWithRequest(httpRequest);
			if (!this.requestEntity.hasBody()) {
				HttpHeaders httpHeaders = httpRequest.getHeaders();
				HttpHeaders requestHeaders = this.requestEntity.getHeaders();
				if (!requestHeaders.isEmpty()) {
					httpHeaders.putAll(requestHeaders);
				}
				if (httpHeaders.getContentLength() == -1) {
					httpHeaders.setContentLength(0L);
				}
			}
			else {
				Object requestBody = this.requestEntity.getBody();
				Class<?> requestType = requestBody.getClass();
				HttpHeaders requestHeaders = this.requestEntity.getHeaders();
				MediaType requestContentType = requestHeaders.getContentType();
				for (HttpMessageConverter<?> messageConverter : restTemplate.getMessageConverters()) {
					if (messageConverter.canWrite(requestType, requestContentType)) {
						if (!requestHeaders.isEmpty()) {
							httpRequest.getHeaders().putAll(requestHeaders);
						}
						if (log.isDebugEnabled()) {
							if (requestContentType != null) {
								log.debug("Writing [" + requestBody + "] as \"" + requestContentType +
										"\" using [" + messageConverter + "]");
							}
							else {
								log.debug("Writing [" + requestBody + "] using [" + messageConverter + "]");
							}
						}
						((HttpMessageConverter<Object>) messageConverter).write(
								requestBody, requestContentType, httpRequest);
						return;
					}
				}
				String message = "Could not write request: no suitable HttpMessageConverter found for request type [" +
						requestType.getName() + "]";
				if (requestContentType != null) {
					message += " and content type [" + requestContentType + "]";
				}
				throw new RestClientException(message);
			}
		}
	}
	
	/**
	 * Request callback implementation that prepares the request's accept headers.
	 */
	private class AcceptHeaderRequestCallback implements RequestCallback {
		private final Type responseType;
		private AcceptHeaderRequestCallback(Type responseType) {
			this.responseType = responseType;
		}

		@Override
		public void doWithRequest(ClientHttpRequest request) throws IOException {
			if (this.responseType != null) {
				Class<?> responseClass = null;
				if (this.responseType instanceof Class) {
					responseClass = (Class<?>) this.responseType;
				}
				List<MediaType> allSupportedMediaTypes = new ArrayList<MediaType>();
				for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
					if (responseClass != null) {
						if (converter.canRead(responseClass, null)) {
							allSupportedMediaTypes.addAll(getSupportedMediaTypes(converter));
						}
					}
					else if (converter instanceof GenericHttpMessageConverter) {
						GenericHttpMessageConverter<?> genericConverter = (GenericHttpMessageConverter<?>) converter;
						if (genericConverter.canRead(this.responseType, null, null)) {
							allSupportedMediaTypes.addAll(getSupportedMediaTypes(converter));
						}
					}
				}
				if (!allSupportedMediaTypes.isEmpty()) {
					MediaType.sortBySpecificity(allSupportedMediaTypes);
					if (log.isDebugEnabled()) {
						log.debug("Setting request Accept header to " + allSupportedMediaTypes);
					}
					request.getHeaders().setAccept(allSupportedMediaTypes);
				}
			}
		}

		private List<MediaType> getSupportedMediaTypes(HttpMessageConverter<?> messageConverter) {
			List<MediaType> supportedMediaTypes = messageConverter.getSupportedMediaTypes();
			List<MediaType> result = new ArrayList<MediaType>(supportedMediaTypes.size());
			for (MediaType supportedMediaType : supportedMediaTypes) {
				if (supportedMediaType.getCharset() != null) {
					supportedMediaType =
							new MediaType(supportedMediaType.getType(), supportedMediaType.getSubtype());
				}
				result.add(supportedMediaType);
			}
			return result;
		}
	}

}

