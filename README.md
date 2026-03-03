# 🔐 Keycloak Starter Kit

A **plug-and-play, reusable** Keycloak + Spring Boot security foundation.  
Clone it, run it, drop your business logic on top. Auth is already done.

---

## 📦 What's Inside

```
keycloak-starter/
├── docker/
│   └── docker-compose.yml          # Keycloak + MariaDB — one command startup
├── realm-config/
│   └── starter-realm.json          # Auto-imported: roles, clients, test users
├── backend/
│   ├── src/main/java/com/keycloak/starter/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java        # JWT + role-based route protection
│   │   │   └── KeycloakAdminConfig.java   # Admin REST client setup
│   │   ├── security/
│   │   │   └── KeycloakJwtAuthConverter.java  # Extracts roles from JWT
│   │   ├── service/
│   │   │   ├── KeycloakAdminService.java  # Create/delete/manage users via API
│   │   │   └── TokenService.java          # Client credentials + refresh token
│   │   └── controller/
│   │       └── Controllers.java           # Public / User / Manager / Admin endpoints
│   ├── src/main/resources/
│   │   └── application.yml               # All config via env vars
│   ├── Dockerfile
│   └── pom.xml
├── postman/
│   └── keycloak-starter.postman_collection.json
└── README.md
```

---

## 🚀 Quick Start (3 commands)

```bash
# 1. Clone
git clone https://github.com/your-org/keycloak-starter.git
cd keycloak-starter

# 2. Start everything
cd docker
docker-compose up -d

# 3. Wait ~30 seconds for Keycloak to start, then test
curl http://localhost:9090/api/public/health
```

That's it. Everything is running.

---

## 🔗 URLs

| Service          | URL                                          |
|-----------------|----------------------------------------------|
| Keycloak Admin  | http://localhost:8080/admin                  |
| Keycloak Realm  | http://localhost:8080/realms/starter-realm   |
| Backend API     | http://localhost:9090                        |

**Keycloak Admin Login:** `admin` / `admin123`

---

## 👥 Pre-configured Test Users

| Username       | Password     | Roles                        |
|---------------|-------------|------------------------------|
| admin-user    | admin123    | ROLE_ADMIN, ROLE_USER        |
| manager-user  | manager123  | ROLE_MANAGER, ROLE_USER      |
| regular-user  | user123     | ROLE_USER                    |

---

## 🌐 API Endpoints

| Endpoint                          | Access             | Description                  |
|----------------------------------|--------------------|------------------------------|
| GET  `/api/public/health`         | Anyone             | Health check                 |
| GET  `/api/public/info`           | Anyone             | App info                     |
| GET  `/api/user/me`               | Any logged-in user | Current user profile         |
| GET  `/api/user/dashboard`        | Any logged-in user | User dashboard               |
| GET  `/api/manager/reports`       | Manager + Admin    | Reports                      |
| GET  `/api/admin/users`           | Admin only         | List all Keycloak users      |
| POST `/api/admin/users`           | Admin only         | Create a new user            |
| DELETE `/api/admin/users/{id}`    | Admin only         | Delete a user                |
| POST `/api/admin/users/{id}/roles/{role}` | Admin only | Assign role to user   |
| GET  `/api/token/client-credentials` | Anyone          | Client credentials flow demo |
| POST `/api/token/refresh`         | Anyone             | Refresh token flow demo      |
| POST `/api/token/introspect`      | Anyone             | Inspect any token            |

---

## 🔄 Auth Flows

### 1. Client Credentials Flow (Service-to-Service)
```
Your Service → POST /token (client_id + client_secret)
            ← access_token (no user, service identity)
```
Use when one backend service needs to call another securely.

```bash
curl -X POST http://localhost:8080/realms/starter-realm/protocol/openid-connect/token \
  -d "grant_type=client_credentials" \
  -d "client_id=postman-client" \
  -d "client_secret=postman-secret-5678"
```

### 2. Refresh Token Flow
```
Client → POST /token (refresh_token)
       ← new access_token + new refresh_token
```
Use when the access token expires (default: 5 minutes). Keycloak rotates the refresh token on each use.

```bash
curl -X POST http://localhost:8080/realms/starter-realm/protocol/openid-connect/token \
  -d "grant_type=refresh_token" \
  -d "client_id=postman-client" \
  -d "client_secret=postman-secret-5678" \
  -d "refresh_token=YOUR_REFRESH_TOKEN"
```

### 3. Password Grant (Testing Only)
```bash
curl -X POST http://localhost:8080/realms/starter-realm/protocol/openid-connect/token \
  -d "grant_type=password" \
  -d "client_id=postman-client" \
  -d "client_secret=postman-secret-5678" \
  -d "username=admin-user" \
  -d "password=admin123"
```

---

## 📬 Postman

Import `postman/keycloak-starter.postman_collection.json` into Postman.

1. Run **Step 1 - Get Token** → token auto-saves to `{{access_token}}`
2. Run any other request — auth header is automatically populated
3. Try the same request with different user tokens to see RBAC in action

---

## 🔌 Using This in Your Project

### Step 1 — Copy the security layer
Take these files into your project:
- `SecurityConfig.java` — configure your URL rules
- `KeycloakJwtAuthConverter.java` — no changes needed
- `KeycloakAdminConfig.java` + `KeycloakAdminService.java` — if you need user management
- `application.yml` — update the env var defaults

### Step 2 — Add your `application.yml` values
```yaml
keycloak:
  url: http://your-keycloak-host:8080
  realm: your-realm-name
  client-id: your-client-id
  client-secret: your-client-secret
```

### Step 3 — Protect your endpoints
```java
// In SecurityConfig.java — add your routes:
.requestMatchers("/api/orders/**").hasAnyRole("ADMIN", "USER")
.requestMatchers("/api/payments/**").hasRole("ADMIN")

// OR use method-level security:
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> sensitiveEndpoint() { ... }
```

### Step 4 — Get current user in any controller
```java
@GetMapping("/my-orders")
public ResponseEntity<?> myOrders(@AuthenticationPrincipal Jwt jwt) {
    String userId = jwt.getSubject();
    String username = jwt.getClaimAsString("preferred_username");
    // use userId to fetch user-specific data
}
```

---

## ⚙️ Configuration Reference

All settings are environment-variable driven — no hardcoded values.

| Env Variable            | Default               | Description                    |
|------------------------|-----------------------|--------------------------------|
| `KEYCLOAK_URL`         | http://localhost:8080 | Keycloak server URL            |
| `KEYCLOAK_REALM`       | starter-realm         | Realm name                     |
| `KEYCLOAK_CLIENT_ID`   | backend-service       | Client ID for this service     |
| `KEYCLOAK_CLIENT_SECRET` | backend-secret-1234 | Client secret                  |

---

## 🏗️ How JWT Validation Works

```
Request → Spring Boot
            ↓
  Extract Bearer token from Authorization header
            ↓
  Fetch Keycloak's public key (JWKS endpoint) — cached automatically
            ↓
  Verify JWT signature + expiry
            ↓
  Extract realm_access.roles → map to Spring GrantedAuthorities
            ↓
  Check if user's roles match the required role for this endpoint
            ↓
  ✅ Allow  or  ❌ 403 Forbidden
```

Spring caches the Keycloak public key — no Keycloak round-trip on every request.

---

## 🔒 Security Notes

- Change `KEYCLOAK_ADMIN_PASSWORD` before deploying
- Use `start` instead of `start-dev` in production Keycloak command
- Set `KC_HOSTNAME` to your actual domain in production
- Enable HTTPS (`KC_HTTPS_ENABLED=true`) in production
- Rotate `client_secret` values per environment

---

## 📋 Pre-configured Roles

| Role          | Description                                      |
|--------------|--------------------------------------------------|
| `ROLE_ADMIN`   | Full access — manage users, roles, resources   |
| `ROLE_MANAGER` | Manage resources, cannot manage users/roles    |
| `ROLE_USER`    | Standard access — own resources only           |

Add more roles in `starter-realm.json` → `roles.realm` array, then restart with `--import-realm`.
