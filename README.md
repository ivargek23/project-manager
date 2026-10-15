# Project manager 

Application for managing projects, tasks with dependencies, subtasks, critical-path computation, burndown metrics, and AI-assisted task description generation from meeting notes.

## Platform choices

- **Hosting**: Render (web service, Frankfurt)
- **Database**: Neon PostgreSQL (Frankfurt)
- **AI provider**: Google Gemini (via Spring AI)

## Domain model

![Domain model](docs/domain_model.drawio.svg)

## Prototype

- Figma file: https://www.figma.com/design/J8fzbXuHwKIUMOLCqgbaSi/Project-and-task-management?node-id=0-1&t=ARnO8CqVVVWQxsEJ-1
- Low-fidelity wireframes: [docs/design/wireframes.pdf](docs/design/wireframes.pdf)

## CI

![Build and test](https://github.com/ivargek23/project-manager/actions/workflows/build-and-test.yml/badge.svg)

## Live application

https://project-manager-i464.onrender.com

Hosted on Render's free plan (Frankfurt) with a Neon PostgreSQL database (Frankfurt). The free service sleeps after about 15 minutes without traffic, so the first request after a pause can take about two minutes while the application starts.