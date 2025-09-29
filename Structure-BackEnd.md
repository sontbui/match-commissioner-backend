# Match Commissioner -- Phase 1 (Backend)

**Stack**: Spring Boot 3.x (Java 17+), Spring Web, Spring Data JPA,
Spring Security (JWT), PostgreSQL, WebSocket/SSE, MapStruct (optional),
Flyway, OpenAPI/Swagger

Sports in scope: **Badminton, Pickleball, Tennis (Single/Double)**;
**Football (Team 5/7/11)**. Volleyball excluded.

------------------------------------------------------------------------

## 1) Domain Model (ERD -- simplified)

``` text
[Tournament]
- id (PK)
- name
- sport_type: BADMINTON | PICKLEBALL | TENNIS | FOOTBALL
- match_type: SINGLE | DOUBLE | TEAM
- team_size: 5|7|11 (nullable, only for FOOTBALL)
- start_date, end_date, status: DRAFT|SCHEDULED|ONGOING|FINISHED

[Tournament] 1..n ---> [Participant]

[Participant]
- id (PK)
- tournament_id (FK)
- type: SINGLE|DOUBLE|TEAM
- display_name (computed from players/team name)

[Player]
- id (PK)
- full_name
- dob (nullable), country (nullable)

[Participant_Player] (for SINGLE/DOUBLE, and can also store football roster)
- participant_id (FK)
- player_id (FK)

[Team] (Football only)
- id (PK)
- name

[Team_Player]
- team_id (FK)
- player_id (FK)

[Match]
- id (PK)
- tournament_id (FK)
- round_name (e.g., Group A, R16, QF, SF, Final)
- participant_a_id (FK)
- participant_b_id (FK)
- start_time, court (nullable), status: SCHEDULED|LIVE|FINISHED

[Match_Score_Event] (real-time updates, optional but recommended)
- id (PK)
- match_id (FK)
- ts, actor (A|B), value (points/goal), meta (JSON)

[Match_Result]
- match_id (PK/FK to Match)
- score_a, score_b (string: "21-18, 18-21, 21-19" or integer for football goals)
- winner_participant_id (FK)
- notes
- report_url (nullable)
```

> Note: Keeping **Participant** generic with a `type` allows one
> scheduling/generation path across all sports.

------------------------------------------------------------------------

## 2) RESTful CRUD -- Endpoint Contract

Base URL prefix: `/api/v1`

### Auth (for Match Commissioner)

-   `POST /auth/login` → { email, password } → JWT
-   `GET /auth/me` → profile

### Tournament

-   `POST /tournaments` → create tournament
-   `GET /tournaments` → list
-   `GET /tournaments/{id}` → detail
-   `PUT /tournaments/{id}` → update (name, dates, config ...)
-   `DELETE /tournaments/{id}`
-   `POST /tournaments/{id}/generate-matches` → run scheduler (RR/KO)
-   `POST /tournaments/{id}/finish` → finalize entire tournament + build
    tournament report

### Participant (Players / Doubles / Teams)

-   `POST /tournaments/{id}/participants` → add participant (payload
    varies by `match_type`)
-   `GET /tournaments/{id}/participants` → list
-   `GET /participants/{pid}` → detail
-   `PUT /participants/{pid}` → update
-   `DELETE /participants/{pid}`

**Helpers**

-   `POST /participants/{pid}/players` → add player(s) into participant
    (single/double or football roster)
-   `DELETE /participants/{pid}/players/{playerId}`
-   `POST /teams` (football only) → create named Team
-   `POST /teams/{teamId}/players` → attach players to team

### Match

-   `GET /tournaments/{id}/matches` → list by tournament
-   `GET /matches/{mid}` → detail
-   `PUT /matches/{mid}` → reschedule/court/status
-   `DELETE /matches/{mid}`

**Real-time score**

-   `POST /matches/{mid}/events` → append score event (point/goal)
    (transitions SCHEDULED→LIVE)
-   `GET /matches/{mid}/events` → fetch history
-   WebSocket: `/ws/live` topic `match.{mid}` to publish score updates
    (or SSE: `/matches/{mid}/stream`)

**Finalize & reports**

-   `PUT /matches/{mid}/finalize` → { scoreA, scoreB, winnerId, notes? }
    → create `Match_Result`
-   `POST /matches/{mid}/report` → (re)generate match PDF → returns {
    reportUrl }

### Report (Tournament)

-   `POST /tournaments/{id}/report` → generate tournament report
    PDF/Excel summary → { reportUrl }

------------------------------------------------------------------------

## 3) Use Cases (Happy Paths)

### UC-01 Create Tournament

1.  Commissioner calls `POST /tournaments` with { name, sport_type,
    match_type, team_size? }
2.  System returns tournament id with status `DRAFT`.

### UC-02 Register Participants

-   **Racket sports (single/double)**: commissioner posts players
    (single) or pairs (double) via
    `POST /tournaments/{id}/participants`.
-   **Football**: commissioner creates participants as **teams** and
    attaches players until size (5/7/11) satisfied.

### UC-03 Generate Matches (Scheduling)

1.  Commissioner triggers `POST /tournaments/{id}/generate-matches` with
    config { format: ROUND_ROBIN\|KNOCKOUT, shuffle: true, courts? }.
2.  System creates `Match` rows with pairings/rounds → tournament status
    `SCHEDULED`.

### UC-04 Live Scoring

1.  During the match, commissioner (or table official) posts events via
    `POST /matches/{mid}/events` (e.g., `{ actor:"A", value:1 }`).
2.  Backend publishes to topic `match.{mid}` via WebSocket; Viewers
    subscribe to see real-time scoreboard.

### UC-05 Finalize Match

1.  At match end, commissioner calls `PUT /matches/{mid}/finalize` with
    final score(s) & `winnerId`.
2.  System sets match `status=FINISHED`, stores `Match_Result`.
3.  System (or user) calls `POST /matches/{mid}/report` → generate Match
    Report PDF.

### UC-06 Finish Tournament

1.  When all matches finished, commissioner calls
    `POST /tournaments/{id}/report` to generate final summary (ranking,
    results, brackets).
2.  Optionally `POST /tournaments/{id}/finish` to mark tournament
    `FINISHED`.

------------------------------------------------------------------------

## 4) Sequence Diagram (text)

``` text
User -> TournamentAPI: POST /tournaments
TournamentAPI -> DB: insert Tournament
User -> ParticipantAPI: POST /tournaments/{id}/participants (xN)
ParticipantAPI -> DB: insert Participant (+ Participant_Player)
User -> TournamentAPI: POST /tournaments/{id}/generate-matches
TournamentAPI -> SchedulingService: build bracket/fixtures
SchedulingService -> DB: insert Match (pairings)

=== Live ===
User -> MatchAPI: POST /matches/{mid}/events {actor, value}
MatchAPI -> DB: insert Match_Score_Event
MatchAPI -> LiveService: publish WebSocket match.{mid}
Viewer <- LiveService: push update

=== Finalize ===
User -> MatchAPI: PUT /matches/{mid}/finalize {scoreA, scoreB, winnerId}
MatchAPI -> DB: upsert Match_Result, update Match.status=FINISHED
User -> ReportAPI: POST /matches/{mid}/report
ReportAPI -> ReportService: create PDF
ReportService -> Storage: upload PDF (return URL)
User <- ReportAPI: {reportUrl}

=== Tournament Report ===
User -> ReportAPI: POST /tournaments/{id}/report
ReportAPI -> ReportService: aggregate standings & results
ReportService -> Storage: upload PDF/Excel
User <- ReportAPI: {reportUrl}
```

------------------------------------------------------------------------

## 5) Scheduling Service -- Algorithms (overview)

-   **Knockout (Single-elim)**: seed participants, handle byes
    (power-of-two), generate rounds R32/R16/QF/SF/Final.
-   **Round Robin**: circle method; for odd number, add BYE; compute
    fixtures per round.
-   **Football group → knockout (optional)**: RR in groups → top N
    advance to KO.

> Store `round_name` and optional `group_name` in `Match` to support
> both styles.

------------------------------------------------------------------------

## 6) Example Payloads

### Create Tournament (Badminton Double)

``` json
POST /api/v1/tournaments
{
  "name": "HCMC Open 2025",
  "sportType": "BADMINTON",
  "matchType": "DOUBLE",
  "startDate": "2025-10-10",
  "endDate": "2025-10-12"
}
```

### Add Participant (Double)

``` json
POST /api/v1/tournaments/{id}/participants
{
  "type": "DOUBLE",
  "players": [
    { "fullName": "Nguyen A" },
    { "fullName": "Tran B" }
  ]
}
```

### Add Football Team (7-a-side)

``` json
POST /api/v1/tournaments/{id}/participants
{
  "type": "TEAM",
  "team": {
    "name": "RND United",
    "players": [
      {"fullName":"Son Bui"}, {"fullName":"Tri Mai"}, {"fullName":"Luan Nguyen"},
      {"fullName":"A"},{"fullName":"B"},{"fullName":"C"},{"fullName":"D"}
    ]
  }
}
```

### Live Score Event

``` json
POST /api/v1/matches/{mid}/events
{
  "actor": "A",  
  "value": 1,
  "meta": { "rally":"smash" }
}
```

### Finalize Match & Report

``` json
PUT /api/v1/matches/{mid}/finalize
{ "scoreA": "21-18, 21-19", "scoreB": "18-21, 19-21", "winnerId": 123 }

POST /api/v1/matches/{mid}/report
```

------------------------------------------------------------------------

## 7) JPA Entities (concise)

... (omitted here for brevity in code)
