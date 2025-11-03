# Javanaise - Distributed Shared Objects System

A distributed shared objects system implementation in Java using RMI (Remote Method Invocation). This project implements a centralized coordinator pattern to manage shared objects across multiple distributed servers with read/write lock synchronization.

## Getting Started

### Branches

- `master`: Stable release version + support for ungraceful client disconnections
- `multi-threading`: Experimental features for concurrent access testing
- `server-cache-limitation`: Experimental features for server-side caching with size limits
- `multi-repartis-objets`: Experimental features for nested distributed objects

### Prerequisites
- SDK 21 or higher
- Gradle (included via wrapper)

### Build the Project

From the `scripts` directory:

```bash
cd scripts
./compil
```

Or from the root directory:

```bash
./gradlew build
```

## Usage

the Coordinator must be launched **before** any servers.

### 1. Start the Coordinator

```bash
cd scripts
./coord
```

The coordinator will start on port 5000 and manage all shared objects.

### 2. Start Local Server(s)

In a separate terminal:

```bash
cd scripts
./server
```

You can launch multiple servers in different terminals to test distributed synchronization.


## Available Commands

Once a server is running, you can interact with it using the following commands:

### Meta Commands

| Command     | Syntax            | Description                                       |
|-------------|-------------------|---------------------------------------------------|
| `help`      | `help`            | Display help information about available commands |
| `print_all` | `print_all` <y/n> | Print all debug logs to console (y/n)             |

### Object Management

| Command         | Syntax                  | Description                                             |
|-----------------|-------------------------|---------------------------------------------------------|
| `create` or `c` | `create <name> [value]` | Create a new shared object with optional initial value  |
| `lookup`        | `lookup <name>`         | Retrieve an existing shared object from the coordinator |
| `list` or `ls`  | `ls`                    | List all local objects and their current values         |

**Examples:**
```bash
create myObject 42      # Create object "myObject" with value 42
create counter          # Create object "counter" with random value
lookup myObject         # Lookup object "myObject" from coordinator
ls                      # List all local objects
```

### Testing Commands

| Command               | Syntax                      | Description                                                                             |
|-----------------------|-----------------------------|-----------------------------------------------------------------------------------------|
| `test`                | `test <name> <value>`       | Add a value to an object and display before/after                                       |
| `cpt`                 | `cpt <count>`               | Increment counter object N times (performance test)                                     |
| `waitwrite` or `ww`   | `ww`                        | Test write lock with 20-second delay                                                    |
| `multithread` or `mt` | `mt <name> <threads>`       | Test concurrent access with N threads (you **must** be on the `multi-threading` branch) |
| `mro`                 | see `multi-repartis-objets` | Instructions for this commands are in the corresponding branch README                   |

_if you encounter deadlocks with multithread tests, start small (2-5 threads) and increase gradually._

**Examples:**
```bash
test myObject 5         # Add 5 to myObject, show before/after
cpt 100                 # Increment "cpt" object 100 times (measures performance)
mt counter 10           # Create 10 threads that increment "counter" simultaneously
ww                      # Test long write lock (blocks for 20 seconds)
```

### System Commands

| Command       | Syntax | Description                      |
|---------------|--------|----------------------------------|
| `exit` or `q` | `exit` | Terminate the server and cleanup |


## Testing Scenarios

### Scenario 1: Basic Object Sharing
1. Start coordinator and two servers
2. On Server 1: `create counter 0`
3. On Server 2: `lookup counter`
4. On Server 1: `test counter 10` → should show 10
5. On Server 2: `test counter 5` → should show 15 (synchronized!)

### Scenario 2: Concurrent Access
1. Start coordinator and one server
2. Run multithread test: `mt test 2`
3. Value should be 2
4. Run multithread test: `mt test 100`
5. Value should be 102

### Scenario 3: Lock Contention
1. Start coordinator and two servers
2. On Server 1: `ww` (locks for 20 seconds)
3. On Server 2: try `ww`
4. Server 2 will wait until Server 1's lock is released

### Scenario 4: Ungraceful Disconnection
1. Start coordinator and two servers
2. On Server 1: `ww` (locks for 20 seconds)
3. On Server 2: try `ww`
4. Kill Server 1 process before 20 seconds elapse
5. Server 2 should proceed after Server 1's disconnection is detected

