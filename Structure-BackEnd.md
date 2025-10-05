# Match Commissioner — Phase 1 (Backend, MongoDB)

**Stack**: Spring Boot 3.x (Java 17+), Spring Web, **Spring Data MongoDB**,  
Spring Security (JWT), WebSocket/SSE, MapStruct (optional),  
Mongock/FlywayDB-for-Mongo (for migration), OpenAPI/Swagger  

**Sports supported:** Badminton, Pickleball, Tennis (Single/Double), Football (Team 5/7/11)

---

## 1) Domain Model (ERD — simplified)

```text
User
  └── creates ──> Tournament
                    └── has ──> TournamentEvent (category: Singles / Doubles / Team; gender: Male/Female/Mixed)
                                  ├── has ──> Team ─── has ──> Player
                                  └── has ──> Match ─── has (audit) ──> ScoreHistory
```

---

## 2) Collections & Fields

### 2.1 users

| Field | Type | Constraints / Notes |
|---|---|---|
| user_id | ObjectId | Auto-generate (Not empty) |
| first_name | string | min 2, max 50 (Not empty) |
| last name | string | min 2, max 100 (Not empty) |
| dob | string (dd/mm/yyyy) | Date format string (Not empty) |
| gender | enum | Male / Female (Not empty) |
| email | string | Must be valid email (Not empty, unique) |
| password | string | Encoded, 6–30 chars, must contain lower/upper/number (Not empty) |
| username | string | 5–30 chars, unique (Not empty) |
| phonenumber | string | digits 10–11 (Not empty) |
| created_at | string (dd/mm/yyyy) | (Not empty) |
| avatar_url | string (url) | Optional |
| last_login | Timestamp | Optional |
| role | enum | ADMIN / REFEREE / MANAGER / USER (default USER) |
| status | enum | ACTIVE / INACTIVE / BANNED (default ACTIVE) |

---

### 2.2 tournament

| Field | Type | Constraints / Notes |
|---|---|---|
| tournament_id | ObjectId | Auto-generate (Not empty) |
| name_tournament | string | min 2, max 512 (Not empty) |
| sport_type | enum | Badminton / Pickle Ball / Tennis / Football (Not empty) |
| start_at | Timestamp | (Not empty) |
| end_at | Timestamp | (Not empty) |
| location | string | Validate later (Not empty) |
| status | enum | UPCOMING / ONGOING / FINISHED (Not empty) |
| created_by | user_id (ref) | (Not empty) |
| teams | [team_id] | Can be empty before registration |
| matches | [match_id] | Can be empty before schedule |
| rule | object | Must include sport-type specific rules (Not empty) |
| allow_gender | enum | Male / Female / ALL (Not empty) |
| prize | object | Optional (overall) |
| sponsors | [string] | Optional |
| logo_url | string (url) | Optional |
| events | [event_id] | Represent event categories (Not empty once created) |

---

### 2.3 tournament_events

| Field | Type | Constraints / Notes |
|---|---|---|
| event_id | ObjectId | Auto-generate (Not empty) |
| tournament_id | tournament_id (ref) | (Not empty) |
| event_name | string | min 2, max 128 (e.g., “Men Singles”, “Mixed Doubles”) (Not empty) |
| type | enum | SINGLE / DOUBLE / TEAM (Not empty) |
| gender | enum | Male / Female / Mixed (Not empty) |
| rule | object | Inherit/override tournament.rule (Not empty) |
| teams | [team_id] | Can be empty before registration |
| matches | [match_id] | Can be empty before schedule |
| prizes | object | gold/silver/bronze/encouragement etc. (Not empty) |
| status | enum | UPCOMING / ONGOING / FINISHED (Not empty) |
| winner_team_id | team_id (ref) | Can be empty until event finished |

---

### 2.4 teams

| Field | Type | Constraints / Notes |
|---|---|---|
| team_id | ObjectId | Auto-generate (Not empty) |
| team_name | string | min 2, max 128 (Not empty) |
| tournament_id | [tournament_id] (ref) | (Not empty; team can join multiple tournaments) |
| event_id | event_id (ref) | (Not empty; identify which event this team belongs to) |
| players | [player_id] (ref) | Must match gender restriction (Not empty) |
| country | string | min 2, max 128 (Not empty) |
| seed | number | Optional |
| coach | player_id (ref) | (Not empty) |
| captain | player_id (ref) | (Not empty) |
| logo_url | string (url) | Optional |

---

### 2.5 players

| Field | Type | Constraints / Notes |
|---|---|---|
| player_id | ObjectId | Auto-generate (Not empty) |
| player_first_name | string | min 2, max 50 (Not empty) |
| player_last_name | string | min 2, max 50 (Not empty) |
| nickname | string | Optional (for display) |
| gender | enum | Male / Female (Not empty) |
| height_cm | double | (Not empty) |
| weight_kg | double | (Not empty) |
| ranking | string / enum | Optional |
| team_id | [team_id] (ref) | One player can join multiple teams (Not empty) |
| urgent_phonenumber | string | digits 10–11 (Optional) |
| photo_url | string (url) | Optional |
| social_link | string (url) | Optional |
| dob | string (dd/mm/yyyy) | Optional |

---

### 2.6 matches

| Field | Type | Constraints / Notes |
|---|---|---|
| match_id | ObjectId | Auto-generate (Not empty) |
| tournament_id | tournament_id (ref) | (Not empty) |
| event_id | event_id (ref) | (Not empty) |
| round | string | e.g., Group A, QF, SF, Final (Not empty) |
| team_a | team_id (ref) | (Not empty) |
| team_b | team_id (ref) | (Not empty) |
| status | enum | SCHEDULE / ONGOING / FINISHED / CANCELLED (Not empty) |
| score | object | sport-based scoring structure (Not empty) |
| filed_number | string | e.g., “Field 3” or “Court A” (Optional) |
| winner_team_id | team_id (ref) | Optional before finish |
| start_time | Timestamp | Optional |
| end_time | Timestamp | Optional |
| referee_id | user_id (ref) | Optional |
| video_url | string (url) | Optional |

---

### 2.7 scores

| Field | Type | Constraints / Notes |
|---|---|---|
| score_id | ObjectId | Auto-generate (Not empty) |
| match_id | match_id (ref) | (Not empty) |
| history | array | Track updates over time |
| sets | array | Example: [ {setNumber:1, teamA:21, teamB:18}, ... ] |
| winner_team_id | team_id (ref) | (Not empty) |
| updated_at | Timestamp | (Not empty) |

---

## 3) RESTful API (v1)

| Resource | Methods | Description |
|---|---|---|
| Auth | POST /auth/login, GET /auth/me | Login / get profile |
| Tournaments | POST /tournaments, GET /tournaments, GET /tournaments/{id}, PUT /tournaments/{id}, DELETE /tournaments/{id} | CRUD tournament |
| Events | POST /tournaments/{id}/events, GET /tournaments/{id}/events | Manage tournament events/categories |
| Teams | POST /teams, GET /teams/{id}, PUT /teams/{id}, DELETE /teams/{id} | CRUD team |
| Players | POST /players, GET /players/{id}, PUT /players/{id}, DELETE /players/{id} | CRUD player |
| Matches | GET /tournaments/{id}/matches, GET /matches/{mid}, PUT /matches/{mid}, DELETE /matches/{mid} | Manage matches |
| Scores / Events | POST /matches/{mid}/events, GET /matches/{mid}/events | Live scoring updates (WebSocket/SSE support) |
| Reports | POST /matches/{mid}/report, POST /tournaments/{id}/report | Generate match/tournament reports |

---

## 4) Typical Use Cases

| UC | Description |
|---|---|
| UC-01 | Create tournament → status UPCOMING |
| UC-02 | Create tournament events (Men Singles, Mixed Doubles, etc.) |
| UC-03 | Register teams/players per event |
| UC-04 | Generate match fixtures automatically |
| UC-05 | Real-time scoring updates via WebSocket/SSE |
| UC-06 | Finalize match → generate PDF report |
| UC-07 | Finish tournament → aggregate event results and publish full report |

---

## 5) Real-time Flow (simplified)

```text
User -> MatchAPI: POST /matches/{mid}/events {actor, value}
MatchAPI -> MongoDB: insert ScoreHistory
MatchAPI -> WebSocketPublisher: send to /topic/match.{mid}
Viewer <- WebSocket: receive updated score
```

---

## 6) Scheduling Logic

| Format | Algorithm | Note |
|---|---|---|
| Knockout | Power-of-2 bracket; handle byes | Generate R32/R16/QF/SF/Final |
| Round Robin | Circle method (odd teams add BYE) | Compute fixtures per round |
| Football group | RR → top N advance → knockout | Group and bracket hybrid |

---

## 7) Example JSON Payloads

### Create Tournament (Badminton Double)
```json
POST /api/v1/tournaments
{
  "name": "HCMC Open 2025",
  "sportType": "Badminton",
  "startAt": "2025-10-10T00:00:00Z",
  "endAt": "2025-10-12T00:00:00Z"
}
```

### Add Event (Mixed Doubles)
```json
POST /api/v1/tournaments/{id}/events
{
  "eventName": "Mixed Doubles",
  "type": "DOUBLE",
  "gender": "Mixed",
  "rule": { "setsToWin": 2, "pointsPerSet": 21 }
}
```

### Add Team to Event
```json
POST /api/v1/events/{eid}/teams
{
  "teamName": "Nguyen/Tran Duo",
  "players": ["p1", "p2"],
  "country": "VN"
}
```

### Live Score Event
```json
POST /api/v1/matches/{mid}/events
{
  "actor": "A",
  "value": 1,
  "meta": { "rally": "smash" }
}
```

### Finalize Match
```json
PUT /api/v1/matches/{mid}/finalize
{
  "scoreA": "21-18, 19-21, 21-17",
  "winnerId": "team1"
}
```
