<!-- $theme: gaia -->

# Vamos Volunteer Identity Platform and Incident Command System
## Making mesh networks functional

          
## Why Vamos?

- #### Portability 
  - Each Vamos node is a Raspberry Pi with a complete version of the server code. Everything can be accessed without any external internet 
- #### Usability
  - No installation required. Can create it's own AP 
- #### Resilience
  - Local network allows for eventual replication while providing full uptime
  - Cloud database based backup and discovery features

- #### Scalability
  - Can be run in a cluster, allowing for many different availibility configurations  
  - Naturally distributed, so it is possible for a node to send events to another node on the network. 
- #### Security
  - Industry standard HTTPS communication
  - Encrypting possible sensitive data at rest. 
- #### Accountability
  - System keeps a full record of events, so that each change can be replayed and researched.

This is all meant to support the National Incident Management System.

##### What is the National Incident Management System (NIMS)? 

NIMS is a comprehensive, national approach to incident management that is applicable at all jurisdictional levels. It is intended to:
- Be applicable across a full spectrum of potential incidents, hazards, and impacts,
regardless of size, location or complexity.
- Improve coordination and cooperation between public and private entities in a variety of
incident management activities.
- Provide a common standard for overall incident management. 

## Installation
If you wish to run your own Vamos node, you will need an environment running Java 11 and a Postgres database with a specific schema.

### Database Schema
Please see akka-persistence-postgresql
