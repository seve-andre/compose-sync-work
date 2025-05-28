# Compose Sync work
This project demonstrates how to implement a periodic background task using Android's WorkManager, specifically focusing on bypassing [PeriodicWorkRequest's minimal interval of 15 minutes restriction](https://developer.android.com/reference/androidx/work/PeriodicWorkRequest).

It allows user-selectable sync rate options (1, 2 or 5 minutes).
