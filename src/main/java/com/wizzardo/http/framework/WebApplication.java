package com.wizzardo.http.framework;

import com.wizzardo.epoll.IOThread;
import com.wizzardo.epoll.SslConfig;
import com.wizzardo.tools.evaluation.Config;
import com.wizzardo.http.*;
import com.wizzardo.http.framework.di.DependencyFactory;
import com.wizzardo.http.framework.di.SingletonDependency;
import com.wizzardo.http.framework.message.MessageBundle;
import com.wizzardo.http.framework.message.MessageSource;
import com.wizzardo.http.framework.template.*;
import com.wizzardo.http.mapping.UrlMapping;
import com.wizzardo.tools.evaluation.EvalTools;
import com.wizzardo.tools.io.FileTools;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by wizzardo on 28.04.15.
 */
public class WebApplication extends HttpServer<HttpConnection> {

    protected Environment environment = Environment.DEVELOPMENT;
    protected Config config;
    protected ResourceTools resourcesTools;

    public WebApplication setEnvironment(Environment environment) {
        checkIfStarted();
        this.environment = environment;
        return this;
    }

    public Environment getEnvironment() {
        return environment;
    }

    protected void onStart() {
        Map environments = (Map) this.config.remove("environments");
        if (environments != null) {
            Map<String, Object> env = (Map<String, Object>) environments.get(environment.shortName);
            if (env != null)
                this.config.merge(env);

            env = (Map<String, Object>) environments.get(environment.name().toLowerCase());
            if (env != null)
                this.config.merge(env);
        }

        List<Class> classes = resourcesTools.getClasses();
        DependencyFactory.get().setClasses(classes);

        File staticResources = resourcesTools.getResourceFile("public");
        if (staticResources != null && staticResources.exists())
            urlMapping.append("/static/*", "static", new FileTreeHandler<>(staticResources, "/static")
                    .setShowFolder(false));

        TagLib.findTags(classes);
        DependencyFactory.get().register(DecoratorLib.class, new SingletonDependency<>(new DecoratorLib(classes)));

        Config server = config.config("server");
        setHost(server.get("host", (String) null));
        setPort(server.get("port", 8080));
        setContext(server.get("context", (String) null));

        super.onStart();
        System.out.println("application has started");
        System.out.println("environment: " + environment);
    }

    protected ResourceTools createResourceTools() {
        File src = new File("src");
        return src.exists() && src.isDirectory() ? new DevResourcesTools() : new LocalResourcesTools();
    }

    protected void init() {
        super.init();
        Holders.setApplication(this);
        resourcesTools = createResourceTools();
        DependencyFactory.get().register(ResourceTools.class, new SingletonDependency<>(resourcesTools));
        DependencyFactory.get().register(UrlMapping.class, new SingletonDependency<>(urlMapping));

        MessageBundle bundle = initMessageSource();
        DependencyFactory.get().register(MessageSource.class, new SingletonDependency<>(bundle));
        DependencyFactory.get().register(MessageBundle.class, new SingletonDependency<>(bundle));

        config = new Config();
        loadConfig("Config.groovy");
    }

    protected MessageBundle initMessageSource() {
        return new MessageBundle()
                .appendDefault("default.boolean.true", "true")
                .appendDefault("default.boolean.false", "false")
                ;
    }

    @Override
    protected Worker<HttpConnection> createWorker(BlockingQueue<HttpConnection> queue, String name) {
        return new WebWorker<>(this, queue, name);
    }

    @Override
    protected IOThread<HttpConnection> createIOThread(int number, int divider) {
        return new WebIOThread<>(this, number, divider);
    }

    @Override
    public ControllerUrlMapping getUrlMapping() {
        return (ControllerUrlMapping) super.getUrlMapping();
    }

    @Override
    protected ControllerUrlMapping createUrlMapping() {
        return new ControllerUrlMapping();
    }

    public Config getConfig() {
        return config;
    }

    public WebApplication loadConfig(String path) {
        resourcesTools.getResourceFile(path, file -> {
            System.out.println("load config from: " + file.getAbsolutePath());
            EvalTools.prepare(FileTools.text(file)).get(config);
        });
        return this;
    }

    @Override
    public void setHost(String host) {
        super.setHost(host);
        config.config("server").put("host", host);
    }

    @Override
    public void setPort(int port) {
        super.setPort(port);
        config.config("server").put("port", port);
    }

    @Override
    public void setSessionTimeout(int sec) {
        super.setSessionTimeout(sec);
        config.config("server").config("session").put("ttl", sec);
    }

    @Override
    public void setIoThreadsCount(int ioThreadsCount) {
        super.setIoThreadsCount(ioThreadsCount);
        config.config("server").put("ioWorkersCount", ioThreadsCount);
    }

    @Override
    public void setWorkersCount(int count) {
        super.setWorkersCount(count);
        config.config("server").put("workersCount", count);
    }

    @Override
    public void setContext(String context) {
        super.setContext(context);
        config.config("server").put("context", context);
    }

    @Override
    public void setTTL(long milliseconds) {
        super.setTTL(milliseconds);
        config.config("server").put("ttl", milliseconds);
    }

    @Override
    public void loadCertificates(SslConfig sslConfig) {
        super.loadCertificates(sslConfig);
        config.config("server").config("ssl").put("cert", sslConfig.getCertFile());
        config.config("server").config("ssl").put("key", sslConfig.getKeyFile());
    }
}
