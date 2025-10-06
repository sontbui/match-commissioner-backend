# Database - Match Commissioner

## users
- **user_id**: make mongo auto generate by Object Id → (Not empty)
- **first_name**: string (min 2 characters, max 50 characters) → (Not empty)
- **last name**: string (min 2 characters, max 100 characters) → (Not empty)
- **dob**: Date time format (dd/mm/yyyy) → (Not empty)
- **gender**: enum (Male / Female) → (Not empty)
- **email**: string with email format → (Not empty)
- **password**: string (encoded by BE security | min 6 max 30 characters | must contain lowercase, uppercase, number) → (Not empty)
- **username**: string (min 5 max 30 characters) → (Not empty)
- **phonenumber**: string (min 10 max 11 | validate on code) → (Not empty)
- **created_at**: Date time format (dd/mm/yyyy) → (Not empty)
- **avatar_url**: url (maybe string | can be empty)
- **last_login**: Timestamp
- **role**: enum (ADMIN, REFEREE, MANAGER, USER) → (default USER)
- **status**: enum (ACTIVE, INACTIVE, BANNED) → (default ACTIVE)

## tournament
- **tournament_id**: make mongo auto generate by Object Id → (Not empty)
- **name_tournament**: string (min 2 max 512 characters) → (Not empty)
- **sport_type**: enum (Badminton, Football, Tennis, Pickle Ball) → (Not empty)
- **start_at**: Timestamp → (Not empty)
- **end_at**: Timestamp → (Not empty)
- **location**: string (Validation later) → (Not empty)
- **status**: enum (UPCOMING, ONGOING, FINISHED) → (Not empty)
- **created_by**: user_id (ref) → (Not empty)
- **teams**: [teams_id] ([ref(teams)]) → (Can be empty before registration)
- **matches**: [match_id] ([ref(matches)]) → (Can be empty before schedule)
- **rule**: object (contains sport_type and specific rule) → (Not empty)
- **allow_gender**: enum (Male / Female / ALL) → (Not empty)
- **prize**: object (Define later for total prize, structure by ranks)
- **sponsors**: array string
- **logo_url**: url (maybe string | can be empty)
- **events**: [event_id] ([ref(tournament_events)]) → (Not empty, represent all categories under this tournament)

## tournament_events
- **event_id**: make mongo auto generate by Object Id → (Not empty)
- **tournament_id**: tournament_id (ref[tournament]) → (Not empty)
- **event_name**: string (min 2 max 128 characters | Example: “Men Singles”, “Mixed Doubles”) → (Not empty)
- **type**: enum (SINGLE, DOUBLE, TEAM) → (Not empty)
- **gender**: enum (Male / Female / Mixed) → (Not empty)
- **rule**: object (inherit or override from tournament.rule) → (Not empty)
- **teams**: [team_id] ([ref(teams)]) → (Can be empty before registration)
- **matches**: [match_id] ([ref(matches)]) → (Can be empty before schedule)
- **prizes**: object (gold, silver, bronze, encouragement, etc.) → (Not empty)
- **status**: enum (UPCOMING, ONGOING, FINISHED) → (Not empty)
- **winner_team_id**: team_id (ref[teams]) → (Can be empty before finish)

## teams
- **team_id**: make mongo auto generate by Object Id → (Not empty)
- **team_name**: string (min 2 max 128 characters) → (Not empty)
- **tournament_id**: [tournament_id] ([ref(tournament)]) → (Not empty, One team can join multiple tournaments at different periods)
- **event_id**: event_id (ref[tournament_events]) → (Not empty, identifies which event the team participates in)
- **players**: [player_id] ([ref(players)]) → (Not empty, gender depends on allow_gender in tournament/event)
- **country**: string (min 2 max 128 characters | validate later) → (Not empty)
- **seed**: number (optional, use for bracket ordering)
- **coach**: player_id (ref[players]) → (Not empty)
- **captain**: player_id (ref[players]) → (Not empty)
- **logo_url**: url (maybe string | can be empty)

## players
- **player_id**: make mongo auto generate by Object Id → (Not empty)
- **player_first_name**: string (min 2 character max 50 characters) → (Not empty)
- **player_last_name**: string (min 2 character max 50 characters) → (Not empty)
- **nickname**: string (min 2 character max 50 characters) → (Can be empty, define later for display)
- **gender**: enum (Male / Female) → (Not empty)
- **height_cm**: double → (Not empty)
- **weight_kg**: double → (Not empty)
- **ranking**: string or enum (Define later, can be national/world ranking) → (Can be empty)
- **team_id**: [team_id] ([ref(teams)]) → (Not empty, one player can join more than one team)
- **urgent_phonenumber**: string (min 10 max 11 | validate later) → (Can be empty)
- **photo_url**: url (maybe string | can be empty)
- **social_link**: url (maybe string | can be empty)
- **dob**: Date time format (dd/mm/yyyy) → (Can be empty if unknown)

## matches
- **match_id**: make mongo auto generate by Object Id → (Not empty)
- **tournament_id**: tournament_id (ref[tournament]) → (Not empty)
- **event_id**: event_id (ref[tournament_events]) → (Not empty)
- **round**: string (Define later | Example: Group A, Quarter Final, Semi Final, Final) → (Not empty)
- **team_a**: team_id (ref[teams]) → (Not empty)
- **team_b**: team_id (ref[teams]) → (Not empty)
- **status**: enum (SCHEDULE, ONGOING, FINISHED, CANCELLED) → (Not empty)
- **score**: object → (Not empty, follow scoring structure per sport)
- **filed_number**: string (can be int or string | Example: “Field 3” or “Court A”) → (Can be empty)
- **winner_team_id**: team_id (ref[teams]) → (Can be empty when not finished / Cannot empty after finished)
- **start_time**: Timestamp → (Can be empty before start)
- **end_time**: Timestamp → (Can be empty before finish)
- **referee_id**: user_id (ref[users]) → (Can be empty before assignment)
- **video_url**: url (maybe string | can be empty for replay storage)

## scores
- **score_id**: make mongo auto generate by Object Id → (Not empty)
- **match_id**: match_id (ref[matches]) → (Not empty)
- **history**: array | Example
  ```json
  "history": [
    { "updated_at": "2025-11-05T15:45:00Z", "score": { "teamA": 1, "teamB": 0 } },
    { "updated_at": "2025-11-05T16:10:00Z", "score": { "teamA": 2, "teamB": 1 } }
  ]
  ```
- **sets**: array | Example
  ```json
  "sets": [
    { "setNumber": 1, "teamA": 21, "teamB": 18 },
    { "setNumber": 2, "teamA": 19, "teamB": 21 },
    { "setNumber": 3, "teamA": 21, "teamB": 17 }
  ]
  ```
- **winner_team_id**: team_id (ref[teams]) → (Not empty)
- **updated_at**: Timestamp → (Not empty)

