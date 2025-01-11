---
title: Development Process
description: How we develop
---

# Development Process
This page is WIP!

## Issues and PRs

- All Issues and PRs are automatically added to the roadmap (Insert link here), and statuses/tags should be kept up to date by the person working on them.
- All _server_ changes should have a PR created, and be reviewed/tested by at least one other person (Admin or Contributor)
  - If the PR Closes an issue, it should be linked to it
- Changes to docs can be committed right to main without review, unless review is appropraite.

## Roadmap and Versioning

- Roadmap statues are kept up to date automatically by devs working on Issues and PRs
- Issues and PRs are added to a milestone with our planned version to release
  - A "Bugfix" Milestone exists for all non-critical bugs, and may cover multiple releases. This is rolled over if a new minor version is released
    - If Minor changes were already merged, these bugfixes should be moved to the next release version as to not release an incomplete milestone.
  - Issues and PRs may be moved between milestones if they are not completed in time, or re-prioritized

# Release Process
1. Run the Update Server action on `main`, deploying to `Test`. Make sure all changes work as expected.
2. Determine what version this release will be, based on the roadmap and included changes (MAJOR.MINOR.PATCH).
3. Create a Github Release with the auto-generated changelog (This may already exist as a draft, make sure you re-generate the changelog if so). 
   This should summarize all changes and contributors. Tags should be formatted as `vX.X.X`
4. Run the Update Server action with the tag of the release
5. Post an announcement in Discord with summarized changes

# Tips for Contributors
- Look for issues marked as both Help Wanted and Ready to Start. Ready to start means that we want this next.
- You can create a PR to fix a Backlog issue, but it may not be accepted/merged until that release is planned.


