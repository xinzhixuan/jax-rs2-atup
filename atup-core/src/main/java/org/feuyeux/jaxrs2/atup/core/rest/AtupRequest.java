package org.feuyeux.jaxrs2.atup.core.rest;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.feuyeux.jaxrs2.atup.core.constant.AtupVariable;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.util.CollectionUtils;

/**
 * ATUP Rest Request
 *
 * @author feuyeux@gmail.com
 * @since 1.0
 * 09/09/2013
 */
public class AtupRequest<R, E> {
    private final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(AtupRequest.class.getName());
	private static final int MAX_LISTENERS = 5;
	private static final int MAX_ITEMS = 10;
    public static final String GET = "GET";
    public static final String DELETE = "DELETE";
    public static final String PUT = "PUT";
    public static final String POST = "POST";
    private ClientConfig clientConfig;
    private boolean isAsync = false;
    private Set<Class<?>> clientRegisters;

    public AtupRequest() {
    }

    public AtupRequest(final ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

	public void useApacheConnector() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(MAX_LISTENERS * MAX_ITEMS);
		cm.setDefaultMaxPerRoute(MAX_LISTENERS * MAX_ITEMS);
		clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, cm);
		clientConfig.connectorProvider(new ApacheConnectorProvider());
	}
	
    public void setAsync(boolean isAsync) {
        this.isAsync = isAsync;
    }

    public void setClientRegisters(final Set<Class<?>> clientRegisters) {
        this.clientRegisters = clientRegisters;
    }

    public E rest(final String method, final String requestUrl, final Class<E> returnType) {
        return rest(method, requestUrl, null, null, null, null, returnType);
    }

    public E rest(final String method, final String requestUrl, final Set<AtupRequestParam> headParams, final Set<AtupRequestParam> queryParams,
                  final MediaType requestDataType, final Class<E> returnType) {
        return rest(method, requestUrl, headParams, queryParams, requestDataType, null, returnType);
    }

    public E rest(final String method, final String requestUrl, final MediaType requestDataType, final R requestData, final Class<E> returnType) {
        return rest(method, requestUrl, null, null, requestDataType, requestData, returnType);
    }

    public E rest(final String method, final String requestUrl, final Set<AtupRequestParam> headParams, final Set<AtupRequestParam> queryParams,
                  final MediaType requestDataType, final R requestData, final Class<E> returnType) {
        if (clientConfig == null) {
            clientConfig = new ClientConfig();
        }
        final Client client = ClientBuilder.newClient(clientConfig);

        if (!CollectionUtils.isEmpty(clientRegisters)) {
            for (final Class<?> clazz : clientRegisters) {
                client.register(clazz);
            }
        }

        WebTarget webTarget = client.target(requestUrl);
        if (!CollectionUtils.isEmpty(queryParams)) {
            for (final AtupRequestParam atupRequestParam : queryParams) {
                webTarget = webTarget.queryParam(atupRequestParam.getKey(), atupRequestParam.getValue());
            }
        }

        final Invocation.Builder invocationBuilder = webTarget.request();
        if (!CollectionUtils.isEmpty(headParams)) {
            for (final AtupRequestParam atupRequestParam : headParams) {
                invocationBuilder.header(atupRequestParam.getKey(), atupRequestParam.getValue());
            }
        }

        javax.ws.rs.core.Response response = null;
        Entity<R> entity;
        switch (method) {
            case GET:
                response = invocationBuilder.get();
                break;
            case DELETE:
                response = invocationBuilder.delete();
                break;
            case PUT:
                entity = Entity.entity(requestData, requestDataType);
                response = invocationBuilder.put(entity);
                break;
            case POST:
                if (isAsync) {
                    final AsyncInvoker async = invocationBuilder.async();
                    entity = Entity.entity(requestData, requestDataType);
                    final Future<E> responseFuture = async.post(entity, returnType);
                    try {
                        return responseFuture.get(AtupVariable.ASYNC_LAUNCH_TEST_TIMEOUT + 2, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.error(e);
                    }
                    break;
                } else {
                    entity = Entity.entity(requestData, requestDataType);
                    response = invocationBuilder.post(entity);
                    break;
                }
            default:
                response = invocationBuilder.get();
        }
        if (response != null) {
            return response.readEntity(returnType);
        } else {
            client.close();
            return null;
        }
    }

    //security
    public void proxy(String proxyUri, String proxyUserName, String proxyPassword) {
        if (this.clientConfig == null) {
            this.clientConfig = new ClientConfig();
        }
        this.clientConfig.property(ClientProperties.PROXY_URI, proxyUri);
        this.clientConfig.property(ClientProperties.PROXY_USERNAME, proxyUserName);
        this.clientConfig.property(ClientProperties.PROXY_PASSWORD, proxyPassword);
    }

    //timeout milliseconds
    public void timeout(int connectTimeout, int readTimeout) {
        if (this.clientConfig == null) {
            this.clientConfig = new ClientConfig();
        }
        if (connectTimeout > 0) {
            this.clientConfig.property(ClientProperties.CONNECT_TIMEOUT, connectTimeout);
        }
        if (readTimeout > 0) {
            this.clientConfig.property(ClientProperties.READ_TIMEOUT, readTimeout);
        }
    }
}
