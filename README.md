## Tech
- Java 15
- Docker
- Spring Boot
- H2

## Data
We have 3 data that we're storing

- Topic
- Subscription
- Message

### Topic
- topic: string PK
- publisherId: string

### Subscription
- topic: string PK FK
- subscriberId: string PK

### Message
- id: PK AUTOINCREMENT
- topic: string FK
- subscriberId: string
- message: string
- timestamp: long

## Flow
### Register
- Publishers can register new topics here
- The registered publisher is the only one allowed to publish messages into that topic

### Subscribe
- Subscribers can subscribe to an existing topic

### Publish
- A registered publisher can publish a message to a topic
- The published message will be received by all current subscribers of that topic
- Published messages can be at most 128 KB as a string with UTF_8 encoding

### Get
- A subscriber can get messages from a topic they're subscribed to
- A subscriber can't get other subscribers' messages, or get messages from unsubscribed topics

### Ack
- A subscriber can ack a message they own
- Acknowledged messages will be deleted. The next time the subscriber attempts to get a message from the acknowledged topic, they will receive the next message based on the message's timestamp in that topic. 

## Cache
- There is a cache implemented for message fetching using in memory LRU cache
- The key is composed of {topic, subscriberId}
- Relevant cache key is invalidated after message ack, or publish action from publisher 

## Unit Test
The provided unit test can be executed to validate the behavior of the app from Controller level and downwards