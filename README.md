# Pokédex Java Angular

Application web full-stack Angular 22 + Spring Boot 3.5 + MySQL.

## Prérequis

- Java 21
- Node.js 24+
- Laragon (MySQL)

## Lancement du projet

### 1. Démarrer Laragon (MySQL)

Assure-toi que Laragon est lancé et que la base `pokedex-java-angular` existe.

### 2. Backend (Spring Boot)

```bash
cd backend
.\mvnw spring-boot:run
```

> Disponible sur `http://localhost:8080`

### 3. Frontend (Angular)

```bash
cd frontend
npm run start
```

> Disponible sur `http://localhost:4200`

---

## Commandes utiles

| Commande                                   | Répertoire  | Description                  |
| ------------------------------------------ | ----------- | ---------------------------- |
| `.\mvnw spring-boot:run`                   | `backend/`  | Lancer Spring Boot           |
| `.\mvnw clean install`                     | `backend/`  | Recompiler le projet Java    |
| `npm run start`                            | `frontend/` | Lancer Angular (avec proxy)  |
| `npm run build`                            | `frontend/` | Build de production Angular  |
| `npx ng generate component components/nom` | `frontend/` | Générer un composant Angular |
| `npx ng generate service services/nom`     | `frontend/` | Générer un service Angular   |

---

## Structure du projet

```
pokedex-java-angular/
├── backend/                  # Spring Boot
│   └── src/main/java/com/pokedex/backend/
│       ├── config/           # Sécurité, JWT, CORS
│       ├── controller/       # Endpoints REST
│       ├── dto/              # Objets de transfert
│       ├── entity/           # Entités JPA (tables)
│       ├── repository/       # Accès base de données
│       └── service/          # Logique métier
├── frontend/                 # Angular
│   └── src/app/
│       ├── components/       # Composants (login, register, dashboard...)
│       └── services/         # Services HTTP
└── README.md
```

---

## Ports

| Service     | Port |
| ----------- | ---- |
| Angular     | 4200 |
| Spring Boot | 8080 |
| MySQL       | 3306 |
