## About

A hopelessly overengineered poor mans TelegramBot: Add searches for ebay Kleinanzeigen.
Get notifications if a new item appears for your search.

This project contains two modules and requires a Redis instance:

1. Simple CRUD API to create SearchJobs via Telegram
2. A Job to stream every SearchJob, perfrom the search and notify the user about new items


# Commands

## Searches

Send your bot commands to manage searches

#### Add


`/add <url>`

Add a search to watch

#### Delete

`/clean`

Delete all searches

`/rm <url>`

Delete an item by its url.

#### Get

`/all`

Get all items in order. 

## Notes

#### Add

`/nadd <templateName> <text>`

get all note names

#### Get

`/nall`

get all note names

`/n <templateName>`

get note text by key

#### Remove

`/nrm <templateName>`

remove note by key




## Configuration

Configuration needs to be provided via environment variables

REDIS_URL - Configure your redis instance ex: redis://localhost
BOT_TOKEN - Token for your Telegram bot, use botfather to create one

