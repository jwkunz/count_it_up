# Count It Up!

Count It Up! is a simple dark-themed web app for counting repeated events and tracking them over time.

## Features

- Increment/decrement live counter
- Commit counter value with timestamp
- Reset counter without clearing history
- Edit or delete log entries
- Plot logged values over time with cubic B-spline fit
- Export log as CSV (`timestamp,value`)
- Import log from CSV
- Persist data locally using IndexedDB (with localStorage fallback)

## Usage

1. Open `counter.html` in your browser.
2. Use **Increment** / **Decrement** to set the current value.
3. Click **Commit** to store the value and timestamp.
4. Select log rows to **Edit Entry** or **Delete**.
5. Use **Export CSV** / **Import CSV** for data portability.

## Storage

- Primary: IndexedDB (`count_it_up_db`)
- Fallback: localStorage
- Legacy cookie data is migrated automatically on first load.

## License

MIT (see [LICENSE](LICENSE)).
