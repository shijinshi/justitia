package cn.shijinshi.fabricmanager.controller.aspect;

import cn.shijinshi.fabricmanager.controller.entity.Response;
import cn.shijinshi.fabricmanager.exception.ContextException;
import cn.shijinshi.fabricmanager.exception.DownloadFileException;
import cn.shijinshi.fabricmanager.exception.IdentityVerifyException;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.helper.ssh.exception.CallShellException;
import cn.shijinshi.fabricmanager.service.utils.file.exception.FileServerException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.github.dockerjava.api.exception.BadRequestException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.ws.rs.ProcessingException;

/**
 * 全局的异常切面，用于处理Controller请求异常
 */
@ControllerAdvice
@ResponseBody
public class ControllerExceptionAspect {
    private static final Logger LOGGER = Logger.getLogger(ControllerExceptionAspect.class);

    /**
     * 200 - Service return an exception message
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(ServiceException.class)
    public Response handleServiceException(ServiceException e) {
        String exceptionMessage = e.getExceptionMessage();
        if (StringUtils.isNotEmpty(exceptionMessage)) {
            return new Response().failure(e.getMessage(), exceptionMessage);
        } else {
            return new Response().failure(e.getMessage());
        }
    }

    /**
     * 200 - Service return an exception message
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(ContextException.class)
    public Response handleContextException(ContextException e) {
        LOGGER.error(e);
        return new Response().failure(e.getMessage());
    }

    /**
     * 200 - Service return an exception message
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(DownloadFileException.class)
    public Response handleDownloadFileException(DownloadFileException e) {
        LOGGER.error(e);
        return new Response().failure(e.getMessage());
    }


    /**
     * 200 - Service return an exception message
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(CallShellException.class)
    public Response handleCallShellException(CallShellException e) {
        LOGGER.warn("Internal server error.", e);
        return new Response().failure(e.getMessage());
    }

    /**
     * 401 - 身份验证失败
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({IdentityVerifyException.class})
    public Response handleIdentityVerifyException(IdentityVerifyException e) {
        LOGGER.info(e);
        return new Response().failure(e.getMessage());
    }

    /**
     * 401 - token验证失败
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(JWTVerificationException.class)
    public Response handleJWTVerificationException(JWTVerificationException e) {
        LOGGER.info(e);
        return new Response().failure(e.getMessage());
    }

    /**
     * 200 - Service return an exception message
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(FileServerException.class)
    public Response handleFileServerException(FileServerException e) {
        LOGGER.info(e);
        return new Response().failure(e.getMessage());
    }

    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Response handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        LOGGER.debug("Could not read json.", e);
        return new Response().failure("Could not read json");
    }

    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public Response handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        LOGGER.debug("Parameter validation exception.", e);
        return new Response().failure("Parameter validation exception.");
    }

    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler({BindException.class})
    public Response handleBindException(BindException e) {
        LOGGER.debug("Parameter bind exception.", e);
        return new Response().failure("Parameter bind exception.");
    }

    /**
     * 405 - Method not allowed
     * 是ServletException的子类，需要Servlet API支持
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Response handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        LOGGER.warn("Request method not supported.", e);
        return new Response().failure("Request method not supported.");
    }

    /**
     * 415 - Unsupported media type
     * 是ServletException的子类，需要Servlet API支持
     */
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    public Response handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        LOGGER.warn("Content type not supported.", e);
        return new Response().failure("Content type not supported");
    }


    /**
     * 500 - Internal server error
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    public Response handleException(Exception e) {
        LOGGER.error("Internal server error.", e);
        return new Response().failure(e.getMessage());
    }


    /**
     * 500 - Internal server error
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(RuntimeException.class)
    public Response handleRuntimeException(RuntimeException e) {
        LOGGER.error("Internal server error.", e);
        if (e instanceof ProcessingException) {
            return new Response().failure("远程主机访问失败");
        }else if (e instanceof BadRequestException){
            return new Response().failure(e.getMessage());
        }else {
            return new Response().failure("Internal server error.");
        }
    }
}
