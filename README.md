
# NautiChart

## Overview
NautiChart is an application designed to provide users with real-time vessel positions, harbours and marinas locations, as well as points of interest. Leveraging data fetched from MarineTraffic API, Overpass API, and Nominatim for address and place searching.

**_Please note that NautiChart is still in development, so some features are not implemented yet._**

<img src="screenshots/preview_1.png" alt="Example Image" width="300"/> <img src="screenshots/preview_2.png" alt="Example Image" width="300"/> <img src="screenshots/preview_3.png" alt="Example Image" width="300"/> <img src="screenshots/preview_4.png" alt="Example Image" width="300"/> <img src="screenshots/preview_5.png" alt="Example Image" width="300"/>

## Features
- **Vessel Tracking**: Latest positions of vessels worldwide.
- **Harbour and Marina Information**: View positions of harbours and marinas.
- **Points of Interest**: Discover various points of interest around you that are part of Open Source OpenStreetMap database.
- **Search Functionality**: Easily search for addresses and other places using Nominatim integration.

## Technologies Used
- **Kotlin, [Coroutines](https://developer.android.com/kotlin/coroutines) & [Flow](https://developer.android.com/kotlin/flow)**
- **MarineTraffic API**: Provides real-time vessel positions and other maritime data.
- **Overpass API**: Fetches points of interest around the user's location.
- **Nominatim**: Used for searching addresses and places.
- **Room**: Persistence library for storing app data.
- **Jetpack DataStore**: Data storage solution from the Android Jetpack libraries.
- **JSoup**: Library for parsing HTML.
- **Retrofit**: A type-safe HTTP client for Android and Java.
- **AppSearch API**: For advanced search capabilities.
- **Gson**: Library for JSON serialization and deserialization.
- **Dagger Hilt**: Dependency injection framework.
- **Coil**: An image loading library for Android and Compose Multiplatform.
- **MVVM** (used for legacy views) & **MVI** (used for composables) architecture
- **WorkManager**: Android library for persistent work
- **Foreground services**: Service that performs work that is visible to the user

## License
**NautiChart** is licensed under the **GNU General Public License (GPL)**. This license ensures that anyone who distributes copies of your app, whether for free or for a fee, must also make the source code available under the same license. This effectively prevents others from selling your app without also providing the source code.

## Contact
mikailodev@gmail.com
