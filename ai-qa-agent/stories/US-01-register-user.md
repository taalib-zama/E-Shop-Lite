# User Story — Register User (US-01)

```yaml
id: US-01
title: Register User
description: As a visitor I want to register so that I can log in.
ac:
  - name: valid_registration
    given: I provide valid name, email, password
    when: I POST /users
    then: 201 Created
  - name: duplicate_email
    given: Email already exists
    when: I POST /users
    then: 409 Conflict
  - name: weak_password
    given: Password does not meet policy
    when: I POST /users
    then: 400 Bad Request
```

Additional notes: The endpoint must not echo the password back.
