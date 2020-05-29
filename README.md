# skyscraper project

This is skyscraper, my EPG data hoarding project.

## What is this?

This is a Java SE Application which parses a DVB Transport Stream and performs data mining on the PSI in order to save EPG Data and frequency lists into a PostgreSQL Database. This is designed to perform in an automated/headless environment.

Please not that this is a work-in-progress and DOES NOT WORK YET.

## How to use this?

At the moment this is not usable at all, but if you want to try messing around with this, please download and install dvb4j first. It is available at https://github.com/feyris-tan/dvb4j

You'll also need to create an sql.properties for the database connection. (see SkyscraperOrm.java for details) I've .gitignore-d this file, because it would contain a password.

## Why did you write this?

Short answer: Loneliness, coding for fun, corona lockdown boredom.

Long answer: Since I was a child, I was fascinated by televison and all the things you can experience with it. When my father put a satellite dish onto the roof, this was like a relevation for me. Sadly, archival of current and historial information about digital televison is extremly scarce. I'd like to change that.
