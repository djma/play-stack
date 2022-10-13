# play-stack

Personal playground for full stack development

# Setup

Steps to create a project from scratch.

1. Set up empty git repo
1. Install maven https://www.baeldung.com/install-maven-on-windows-linux-mac#installing-maven-on-mac-os-x
1. Maven quick start https://maven.apache.org/archetypes/maven-archetype-quickstart/
1. Deploy using `git subtree push --prefix java/playstack heroku master` https://stackoverflow.com/questions/43835247/heroku-rails-change-root-directory
1. Force push with `` git push heroku `git subtree split --prefix java/playstack master`:master --force ``
