package com.own.face.util.base;

import com.own.face.util.Resp;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AopControllerTest {

    @Test
    public void returnsControllerResultWhenNoRequestContextIsBound() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        Resp expected = new Resp("payload");
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("ExampleController");
        when(signature.getName()).thenReturn("example");
        when(joinPoint.proceed()).thenReturn(expected);

        assertSame(expected, new AopController().HandleResp(joinPoint));
    }
}
