# Contributing to Clearly

## Before making changes

1. Create a feature branch.
2. Copy `.env.example` to `.env` and add local values only there.
3. Keep unrelated work out of the same commit.

## Code organization

- Keep each storefront page's HTML, CSS, and JavaScript under the same base name.
- Put reusable navigation, commerce, state, or card behavior in the existing shared modules.
- Store media in the appropriate subfolder under `frontend/assets/`.
- Add backend behavior to the service that owns the domain instead of duplicating it across services.
- Never commit credentials, generated logs, `target/`, temporary output, or local IDE folders.

## Verification

- Check desktop and mobile layouts for frontend changes.
- Confirm all linked images and scripts return successfully.
- Run the affected service tests or at least its Maven compile step.
- Verify that checkout, authentication, and notification changes fail safely when optional credentials are absent.

See [`docs/PROJECT_STRUCTURE.md`](docs/PROJECT_STRUCTURE.md) for the repository map.
