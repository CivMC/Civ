# Contributing

We are grateful you have decided to contribute to the CivMC repository and become part of the over 14-year history
that is Civ. There are a few things you should know before making your first contribution.

We would greatly appreicate if you tested your changes, ideally using the Docker setup in this repository using
`docker compose up paper proxy -d`. This will start up a single server on localhost.

Keep in mind that if you are making any config changes, you should change the default plugin config in the resources
folder, but also update the configs for the relevant server(s) if necessary. `gamma` is the code name for CivMC mini,
and most changes applied to main should also apply to mini. You can find these configs in `deployment/files`.

Default config changes will not affect the docker server, but deployment config changes will. If you make any config changes in
the `deployment` directory, simply restart the server `docker compose restart paper` to apply them.

If you make any code changes, run `./gradlew build` and then restart the server.

## Submitting a PR

Please attempt to keep your changesets minimal and easy to review: don't create a massive diff or change anything
out of scope - unless there is some obvious benefit - without calling out why.

In your description, you should list a summary of your changes and *why* you have made those changes. This helps put
it in context for us to review.

**Important:** We have a strict LLM disclosure policy. Please disclose whether you used LLMs and which model you used
in your PR description. Failure to do this will cause your PR to be closed and you to be blocked from contributing
to the repository.
