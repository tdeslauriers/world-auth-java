package world.deslauriers;

import io.dekorate.docker.annotation.DockerBuild;
import io.dekorate.kubernetes.annotation.*;
import io.micronaut.runtime.Micronaut;


@KubernetesApplication(
        name = "auth",
        serviceType = ServiceType.NodePort,
        expose = true,
        host = "localhost",
        replicas = 3,
        imagePullPolicy = ImagePullPolicy.Always,
        labels = @Label(key = "app", value = "auth"),
        ports = @Port(name = "http", containerPort = 8080),
        envVars = {
                @Env(name = "AUTH_JDBC_URL", configmap = "auth-svc-config", value = "jdbc_url"),
                @Env(name = "AUTH_JDBC_USERNAME", configmap = "auth-svc-config", value = "jdbc_username"),
                @Env(name = "AUTH_JDBC_DRIVER", configmap = "auth-svc-config", value = "jdbc_driver"),
                @Env(name = "AUTH_JDBC_DIALECT", configmap = "auth-svc-config", value = "jdbc_dialect"),
                @Env(name = "CORS_URLS", configmap = "auth-svc-config", value = "cors_urls"),
                @Env(name = "AUTH_JDBC_PASSWORD", secret = "auth-mariadb-galera", value = "mariadb-password"),
                @Env(name = "JWT_GENERATOR_SIGNATURE_SECRET", secret = "jwt", value = "signature-pw")
        }
)
@DockerBuild(group = "tdeslauriers", name = "auth")
public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
