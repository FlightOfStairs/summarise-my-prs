This is a tool to summarize work that I've done over the last few years by looking at pull requests opened.

## Usage

Create `.env` by copying `.env.example` and providing openai key.

Extract pull requests for a user using the [GitHub CLI](https://cli.github.com/):

```bash
gh search prs --author="@me" --limit=1000 --json repository,title,state,createdAt,body > prs.json
```

If you have more than 1000 PRs, you'll need to run this multiple times (e.g., filtering with `--created="<=2023-03-15"`), then manually combine.

Run the tool:

```bash
./gradlew run --args "prs.json"
```

This will output ~3 bullet points per month. During generation, previous months are fed in as additional context.
