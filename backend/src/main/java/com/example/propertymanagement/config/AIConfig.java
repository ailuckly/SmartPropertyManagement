package com.example.propertymanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.theokanning.openai.service.OpenAiService.*;

/**
 * AI服务配置类
 */
@Slf4j
@Configuration
public class AIConfig {

    private final AIProperties aiProperties;

    public AIConfig(AIProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    /**
     * 创建OpenAI服务Bean
     * 仅在app.ai.enabled=true时启用
     */
    @Bean
    @ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "true")
    public OpenAiService openAiService() {
        String baseUrl = aiProperties.getBaseUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        
        log.info("初始化OpenAI服务: baseUrl={}, model={}", baseUrl, aiProperties.getModel());
        
        try {
            // 创建自定义拦截器来添加Authorization header
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + aiProperties.getApiKey())
                            .header("Content-Type", "application/json")
                            .method(original.method(), original.body())
                            .build();
                        return chain.proceed(request);
                    }
                })
                .connectTimeout(aiProperties.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(aiProperties.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(aiProperties.getTimeout(), TimeUnit.SECONDS)
                .build();
            
            // 配置Retrofit
            ObjectMapper mapper = defaultObjectMapper();
            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
            
            // 创建OpenAiService实例 - 使用反射获取API接口
            com.theokanning.openai.client.OpenAiApi api = retrofit.create(com.theokanning.openai.client.OpenAiApi.class);
            return new OpenAiService(api);
            
        } catch (Exception e) {
            log.error("初始化OpenAI服务失败", e);
            throw new RuntimeException("Failed to initialize OpenAI service", e);
        }
    }
}
