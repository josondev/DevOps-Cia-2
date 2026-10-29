# Technical Report: End-to-End Cloud CI/CD Pipeline Design

## 1. Problem definition

Modern software teams often follow manual release workflows that involve building the Java artifact locally, running tests, creating containers, and deploying the application by hand. This process is both slow and error-prone. It is difficult to guarantee consistent deployments and the same code version reaching production across different environments. The challenge is to automate the whole software delivery lifecycle so that any validated code change can be delivered to a cloud-hosted environment without manual intervention.

The project requirement is to create a real CI/CD pipeline for a Java web application using a cloud provider, Git, Maven, Docker, and Kubernetes. The solution should include automated build and test stages, code checkout, image publishing, and deployment to a managed Kubernetes cluster. It should also produce supporting deployment files and documentation.

## 2. Architecture overview

The application is a lightweight Spring Boot service that exposes a simple homepage, health endpoint, and version endpoint. The pipeline is implemented with GitHub Actions and runs when code is pushed to the main branch. The toolchain includes Java 17 for compilation, Maven for dependency management and test execution, Docker for containerization, and Kubernetes for orchestration.

The deployment target is Azure Kubernetes Service (AKS), which is a managed Kubernetes service in Microsoft Azure. The deployment workflow authenticates to Azure using GitHub OIDC federation, updates the cluster context, and applies the Kubernetes manifests. This avoids keeping a long-lived Azure client secret in GitHub. The service exposes the app through a Load Balancer so it can be accessed externally.

## 3. Tools selected and justification

- GitHub: provides source control and CI trigger events.
- Java 17 and Maven: standard build and dependency tooling for Java applications.
- Spring Boot: reduces boilerplate and gives a clean REST API framework.
- Docker: ensures the application runs consistently across environments.
- Kubernetes: provides container orchestration, scaling, and health checks.
- Azure AKS: managed Kubernetes reduces administration overhead and is suitable for cloud deployment.
- GitHub Actions: suitable, low-overhead CI/CD platform integrated with GitHub.
- Ansible: useful for repeatable deployment automation and infrastructure configuration tasks.

This toolset is highly compatible with the requirements and is widely used in production-ready DevOps workflows.

## 4. CI/CD pipeline stages

The pipeline is configured in `.github/workflows/ci-cd.yml` and includes the following stages:

1. Checkout source code.
2. Set up the Java environment.
3. Run unit tests with Maven.
4. Package the application as a JAR.
5. Build Docker image.
6. Push image to GitHub Container Registry.
7. Authenticate to Azure.
8. Apply Kubernetes manifests.
9. Wait for rollout completion.
10. Verify the new version is running.

Each stage is placed in order so that no deployment occurs if tests fail. This is a core DevOps principle: a validated build should be the only artifact considered for deployment.

## 5. Cloud resources and deployment approach

The deployment architecture includes the following components:

- GitHub repository: source of truth for application code and workflow definitions.
- GitHub Container Registry: stores the built Docker image.
- Azure Resource Group: container for AKS resources.
- Azure AKS cluster: hosts the application containers.
- Kubernetes Deployment: defines the desired number of pods.
- Kubernetes Service: exposes the application to external traffic using a LoadBalancer.

The application is deployed in the `cicd-demo` namespace. The deployment uses two replicas for resilience and declares readiness and liveness probes so Kubernetes only sends traffic to healthy containers. The manifest repository is versioned along with the app code, making the deployment state reproducible.

## 6. Implementation details

The repository contains these key files:

- `pom.xml`: Maven project definition and Spring Boot configuration.
- `src/main/java/com/example/cicd/CicdDemoApplication.java`: main Spring Boot entry point.
- `src/main/java/com/example/cicd/AppController.java`: REST endpoints.
- `src/test/java/com/example/cicd/AppControllerTest.java`: unit and integration-style tests.
- `Dockerfile`: multi-stage build for a Java container image.
- `k8s/deployment.yaml`: Kubernetes deployment definition.
- `k8s/service.yaml`: external service definition.
- `ansible/playbook.yml`: example deployment automation script.
- `.github/workflows/ci-cd.yml`: CI/CD workflow definition.

The version endpoint helps validate that the desired code change is being served by the running cluster. This makes the demonstration easy to understand during a live demo.

## 7. Testing and verification evidence

The application has automated checks for the following behaviors:

- root endpoint returns a success message
- health endpoint reports service health
- version endpoint returns the app version and metadata

These tests are executed during the GitHub Actions workflow before the Docker image is built. This ensures that broken code does not reach the registry or the cluster.

The local validation command used during development was:

```bash
mvn -B clean test
```

This validates the Spring Boot application and confirms the CI workflow will pass on a clean environment.

## 8. Challenges and solutions

One challenge in cloud-based deployments is granting the pipeline the correct permissions. In production, the workflow should use a service principal with least privilege or provider federation via GitHub OIDC. This avoids hardcoded credentials and reduces operational risk.

Another challenge is making Kubernetes deployment consistent. Using manifests stored in the repository and applying them in the GitHub workflow ensures the same configuration is used across environments and that updates are traceable.

## 9. Security considerations and limitations

Production systems should include the following measures:

- short-lived GitHub OIDC tokens instead of stored Azure client secrets
- secret management via GitHub Secrets, Azure Key Vault, or Kubernetes Secrets
- image scanning and vulnerability analysis
- private registry access for production deployments
- TLS/ingress configuration for public workloads
- RBAC restrictions for cluster access
- environment-specific configuration with configuration maps or secrets

This project is a practical training implementation and does not include a live Azure subscription or actual cloud credentials. It is designed to be executable once the required Azure secrets and cluster names are added by the user or instructor.

## 10. Live demonstration plan

To demonstrate the workflow end-to-end, the following steps can be followed:

1. Commit a change to the app or API response.
2. Push to the `main` branch.
3. Observe the GitHub Actions workflow automatically trigger.
4. Confirm Maven tests pass.
5. Confirm the Docker image is pushed to the registry.
6. Confirm the AKS deployment is updated.
7. Visit the service endpoint in a browser to see the new change.

This proves that a code change can pass through the whole pipeline and appear in the running application automatically.

## 11. Conclusion

The implemented solution satisfies the assignment requirements by combining Git, Maven, Docker, Kubernetes, CI/CD automation, and cloud deployment. It is structured for real-world use, is easy to extend, and can be adapted to an Azure, AWS, or GCP deployment target. The result is an end-to-end, repeatable pipeline that demonstrates the value of automation in modern software delivery.
