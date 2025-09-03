# Restaurant Reservation System

## Architecture

```
┌─────────────────┐    ┌─────────────────┐
│  Load Balancer  │────│   API Gateway   │
│    (Nginx)      │    │   (JWT Auth)    │
└─────────────────┘    └─────────────────┘
                                │
                    ┌───────────┴───────────┐
                    │                       │
          ┌─────────▼────────┐    ┌─────────▼────────┐
          │ Restaurant       │    │ Reservation      │
          │ Service          │    │ Service          │
          │ (Multi-instance) │    │ (Multi-instance) │
          └──────────────────┘    └──────────────────┘
                    │                       │
          ┌─────────▼────────┐    ┌─────────▼────────┐
          │ H2 Database      │    │ H2 Database      │
          │ (Restaurant)     │    │ (Reservation)    │
          └──────────────────┘    └──────────────────┘
```

## Fonctionnalités

### Fonctionnalités Métier
- **Gestion des Restaurants** : CRUD complet pour restaurants, tables et disponibilités
- **Système de Réservation** : Cycle de vie complet des réservations (création, confirmation, annulation, finalisation)
- **Gestion des Clients** : Profils clients et historique des réservations
- **Disponibilité en Temps Réel** : Vérification de disponibilité des tables
- **Communication Inter-Services** : Coordination automatique entre services

### Fonctionnalités Techniques
- **Architecture Microservices** : Services faiblement couplés et déployables indépendamment
- **Architecture Hexagonale** : Séparation claire des responsabilités avec ports et adapters
- **Authentification JWT** : Accès sécurisé avec autorisation basée sur les rôles
- **API HATEOAS** : APIs REST hypermedia avec liens de navigation
- **Load Balancing** : Haute disponibilité avec instances multiples
- **Circuit Breakers** : Patterns de résilience pour la tolérance aux pannes
- **API Gateway** : Point d'entrée unique avec routage et authentification
- **Tests Complets** : Tests unitaires, d'intégration et de contrats
- **Support Docker** : Containerisation complète avec docker-compose
- **Documentation API** : Swagger/OpenAPI avec interface interactive

##  Stack Technique

### Technologies Core
- **Java 17** : Fonctionnalités Java modernes et améliorations de performance
- **Spring Boot 3.2.5** : Dernier Spring Boot avec support de compilation native
- **Spring Cloud Gateway** : API Gateway réactive avec load balancing
- **Spring Security** : Authentification et autorisation basées sur JWT
- **Spring Data JPA** : Persistance de données avec Hibernate
- **Base de données H2** : Base de données en mémoire pour le développement

### Infrastructure
- **Docker & Docker Compose** : Containerisation et orchestration
- **Nginx** : Load balancer et reverse proxy
- **Liquibase** : Gestion des migrations de base de données
- **Maven** : Gestion des dépendances et automatisation de build

### Tests
- **JUnit 5** : Framework de tests unitaires
- **Testcontainers** : Tests d'intégration avec bases de données réelles
- **AssertJ** : Bibliothèque d'assertions fluides

### Documentation & Monitoring
- **Swagger/OpenAPI** : Documentation API interactive
- **Spring Boot Actuator** : Health checks et métriques
- **Prometheus & Grafana** : Monitoring et dashboards (optionnel)

## 📋 Prérequis

- **Java 17** ou supérieur
- **Maven 3.8+**
- **Docker & Docker Compose**
- **Git**

## 🚀 Démarrage Rapide

### 1. Cloner le Repository
```bash
git clone https://github.com/your-org/restaurant-reservation-system.git
cd restaurant-reservation-system
```

### 2. Builder les Applications
```bash
# Builder tous les services
mvn clean package -DskipTests

# Ou builder les services individuellement
mvn clean package -pl restaurant-service -am
mvn clean package -pl reservation-service -am
mvn clean package -pl api-gateway -am
```

### 3. Lancer avec Docker Compose

#### Environnement de Développement (Instances Simples)
```bash
# Démarrer l'environnement de développement
docker-compose -f docker-compose.dev.yml up -d

# Voir les logs
docker-compose -f docker-compose.dev.yml logs -f
```

#### Environnement de Production (Load Balanced)
```bash
# Démarrer l'environnement de production
docker-compose up -d

# Voir les logs
docker-compose logs -f
```

### 4. Vérifier le Déploiement
```bash
# Vérifier la santé des services
curl http://localhost/actuator/health

# Vérifier les services individuels
curl http://localhost:8081/actuator/health  # Restaurant Service 1
curl http://localhost:8082/actuator/health  # Restaurant Service 2
curl http://localhost:8083/actuator/health  # Reservation Service 1
curl http://localhost:8084/actuator/health  # Reservation Service 2
curl http://localhost:8080/actuator/health  # API Gateway
```

## Documentation API

### Accéder à Swagger UI
[![Documentation de l'API Complète](https://img.shields.io/badge/📚_Documentation_API-Swagger_Editor-85EA2D?style=for-the-badge&logo=swagger&logoColor=white)](https://editor.swagger.io/?url=https://raw.githubusercontent.com/matta971/restaurant-test/tree/main/openapi.yml)

### Authentification

#### Obtenir un Token JWT
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer",
    "password": "customer123"
  }'
```

#### Utiliser le Token dans les Requêtes
```bash
curl -X GET http://localhost:8080/api/reservations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Identifiants de Test
| Username | Password | Rôle | Permissions |
|----------|----------|------|-------------|
| `admin` | `admin123` | ADMIN | Accès complet à toutes les opérations |
| `customer` | `customer123` | CUSTOMER | Créer/voir ses propres réservations |
| `restaurant` | `restaurant123` | RESTAURANT_OWNER | Gérer les données du restaurant |

## Exécuter les Tests

### Tests Unitaires
```bash
# Tous les services
mvn test

# Service spécifique
mvn test -pl restaurant-service
mvn test -pl reservation-service
mvn test -pl api-gateway
```

### Tests d'Intégration
```bash
# Tests d'intégration (nécessite Docker)
mvn verify -P integration-tests

# Ou lancer avec Testcontainers
mvn test -Dtest=*IntegrationTest
```

### Tests de Charge
```bash
# Utiliser Apache Bench
ab -n 1000 -c 10 http://localhost/api/restaurants

# Utiliser curl en boucle
for i in {1..100}; do
  curl -s http://localhost/api/restaurants > /dev/null
  echo "Request $i completed"
done
```


### Principes d'Architecture

#### Architecture Hexagonale (Ports & Adapters)
Chaque service suit l'architecture hexagonale :
```
src/main/java/com/restaurant/service/
├── domain/
│   ├── model/          # Entités métier
│   ├── port/
│   │   ├── in/         # Ports d'entrée (use cases)
│   │   └── out/        # Ports de sortie (repositories, clients)
│   └── service/        # Implémentation de la logique métier
├── application/
│   └── config/         # Configuration d'application
└── infrastructure/
    ├── adapter/
    │   ├── in/web/     # Contrôleurs REST
    │   ├── out/persistence/  # Repositories JPA
    │   └── out/client/ # Clients de services externes
    └── config/         # Configuration d'infrastructure
```

#### Test-Driven Development (TDD)
1. **Red** : Écrire un test qui échoue
2. **Green** : Implémenter le code minimal pour que le test passe
3. **Refactor** : Améliorer le code tout en gardant les tests au vert

### Ajouter de Nouvelles Fonctionnalités


### Qualité du Code

#### Style de Code
- Suivre le Google Java Style Guide
- Utiliser Lombok pour réduire le code boilerplate
- Préférer la composition à l'héritage
- Écrire du code auto-documenté

#### Stratégie de Tests
- **Tests Unitaires** : Tester la logique métier en isolation
- **Tests d'Intégration** : Tester les intégrations des adapters
- **Tests de Contrats** : Tester la communication entre services
- **Tests End-to-End** : Tester des parcours utilisateur complets

## 🔧 Configuration

### Variables d'Environnement

#### API Gateway
| Variable | Défaut | Description |
|----------|---------|-------------|
| `JWT_SECRET` | `mySecretKey2024...` | Secret de signature JWT |
| `JWT_EXPIRATION` | `86400` | Expiration du token (secondes) |
| `SERVICES_RESTAURANT_URL` | `http://localhost:8081` | URL du service restaurant |
| `SERVICES_RESERVATION_URL` | `http://localhost:8082` | URL du service réservation |

#### Restaurant Service
| Variable | Défaut | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:restaurant_db` | URL de connexion à la base |
| `SPRING_DATASOURCE_USERNAME` | `restaurant` | Nom d'utilisateur de la base |
| `SPRING_DATASOURCE_PASSWORD` | `password` | Mot de passe de la base |
| `SPRING_H2_CONSOLE_ENABLED` | `true` | Activer la console H2 |
| `SPRING_LIQUIBASE_CHANGE_LOG` | `classpath:db/changelog/...` | Chemin du changelog Liquibase |

#### Reservation Service
| Variable | Défaut | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:reservation_db` | URL de connexion à la base |
| `RESTAURANT_SERVICE_URL` | `http://localhost:8081` | URL du service restaurant |
| `FEIGN_CLIENT_TIMEOUT_READ` | `5000` | Timeout de lecture Feign (ms) |
| `FEIGN_CLIENT_TIMEOUT_CONNECT` | `2000` | Timeout de connexion Feign (ms) |

### Profils d'Application

#### Development (`dev`)
- Logging de debug activé
- Console H2 accessible
- CORS permissif
- Hot reloading activé

#### Docker (`docker`)
- Niveaux de logging de production
- Service discovery via noms de conteneurs
- Optimisé pour déploiement containerisé

#### Test (`test`)
- Bases de données en mémoire
- Services externes mockés
- Configuration de démarrage rapide

### Configuration du Load Balancer

Le load balancer Nginx fournit :
- **Load balancing round-robin** entre instances de services
- **Health checks** avec basculement automatique
- **Rate limiting** (10 requêtes/seconde par IP)
- **Terminaison SSL** (certificat requis)
- **Compression Gzip** pour les réponses API
- **Headers de sécurité** (CORS, protection XSS)

## 📊 Monitoring & Observabilité

### Health Checks
```bash
# Santé globale du système
curl http://localhost/health

# Santé des services individuels
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8080/actuator/health
```

### Métriques
```bash
# Métriques des services
curl http://localhost:8081/actuator/metrics
curl http://localhost:8082/actuator/metrics
curl http://localhost:8080/actuator/metrics

# Routes de la gateway
curl http://localhost:8080/actuator/gateway/routes
```

### Logging
```bash
# Voir les logs
docker-compose logs -f

# Logs spécifiques aux services
docker-compose logs -f restaurant-service-1
docker-compose logs -f reservation-service-1
docker-compose logs -f api-gateway
docker-compose logs -f load-balancer
```

## Déploiement

### Développement Local
```bash
mvn clean package -DskipTests
docker-compose -f docker-compose.dev.yml up -d
```

### Déploiement en Production
```bash
mvn clean package
docker-compose up -d

# Scaler les services
docker-compose up -d --scale restaurant-service-1=3
docker-compose up -d --scale reservation-service-1=2
```

#### Problèmes de Load Balancer
```bash
# Vérifier la configuration nginx
docker exec restaurant-load-balancer nginx -t

# Redémarrer nginx
docker-compose restart load-balancer

# Vérifier le statut des upstreams
curl http://localhost/nginx_status
```

### Optimisation des Performances

#### Options JVM
```bash
# Ajouter à docker-compose.yml environment
- JAVA_OPTS=-Xmx512m -Xms256m -XX:+UseG1GC
```

#### Optimisation Base de Données
```bash
# Optimisation des performances H2
- SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;CACHE_SIZE=10000
```

#### Pool de Connexions
```bash
# Configuration HikariCP
- SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=10
- SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=5
```


### Bonnes Pratiques de Sécurité Implémentées
- Mots de passe hachés avec BCrypt
- Tokens JWT avec temps d'expiration
- Configuration sensible externalisée
- Pas de secrets dans le code source
- Conteneurs Docker exécutés en tant qu'utilisateur non-root
- Segmentation réseau avec réseaux Docker

## Décisions Techniques & Justifications

### Pourquoi Spring Cloud Gateway plutôt que Zuul ?
- **Stack réactive** : Construit sur Spring WebFlux pour de meilleures performances
- **Approche moderne** : Développement actif et pérenne
- **Meilleure intégration** : Support natif Spring Boot 3.x
- **Performance** : I/O non-bloquante pour un débit élevé

### Pourquoi Liquibase plutôt que Flyway ?
- **Support XML/YAML** : Plus flexible que les migrations SQL uniquement
- **Capacités de rollback** : Meilleur support pour les rollbacks en production
- **Logique conditionnelle** : Logique de migration spécifique à la base de données
- **Intégration Spring Boot** : Intégration transparente avec Spring Boot

### Pourquoi Testcontainers plutôt que des Mocks ?
- **Intégration réelle** : Les tests s'exécutent contre des instances de services réelles
- **Validation de contrats** : Assure que les services communiquent correctement
- **Parité d'environnement** : Cohérence développement/test/production
- **Confiance** : Confiance plus élevée dans la préparation au déploiement

### Pourquoi l'Architecture Hexagonale ?
- **Testabilité** : Facile de tester la logique métier en isolation
- **Flexibilité** : Les décisions technologiques peuvent changer sans affecter les règles métier
- **Maintenabilité** : Séparation claire des responsabilités
- **Focus domaine** : Les règles métier ne sont pas couplées aux frameworks