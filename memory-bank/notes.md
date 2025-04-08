# Notes: WakeLock Detail Screen Implementation
*Created: 2023-08-05*
*Updated: 2023-08-05*

## Development Notes
- The app follows MVVM architecture with Repository pattern for data access
- TopAppBar is implemented globally at the app level, not in individual screens
- Navigation uses the Navigation Compose library with route-based navigation
- The prototype design uses Material Design 3 components and styling
- The existing app structure uses ViewModels with StateFlow for state management
- Current data structure uses DAItem for basic item information and InfoEvent for history
- We'll create a new DAInfoRepository to handle JSON-based description information

## Key Design Decisions
- DAInfoRepository will fetch information from a JSON file with fallback mechanisms
- DADetailViewModel will not take constructor parameters for specific items, but use a loadDADetail() method
- Timeline chart will be implemented using Canvas for custom drawing
- All UI state will be immutable and flow from ViewModel to UI
- Settings changes will be applied immediately through the repository

## Implementation Approach
- Follow existing naming conventions and package structure
- Keep component structure relatively flat, avoiding over-fragmentation
- Use LazyColumn for the main content with card-based sections
- Implement custom Canvas-based components only where necessary

## Data Structure Notes
- JSON structure will include: id, name, type, package, safeToBlock, description, recommendation, warning
- Timeline data will represent 24 hours with total/blocked counts per hour
- Statistics will include: count, blockedCount, totalTime, savedTime
- savedTime calculation: (totalTime / allowed events count) * blocked count

## UI Component Structure
- DADetailScreen (main container)
- Header section with icon and name/package
- Statistics card with four metrics
- Information card with description and recommendations
- Settings card with toggles and input
- Timeline chart showing 24-hour activity
- Recent activity list showing detailed events

## Code Snippets
### DADetailState structure
```kotlin
sealed class DADetailState {
    object Loading : DADetailState()
    data class Success(
        val daItem: DAItem,
        val info: DAInfoEntry?,
        val statistics: DAStatistics,
        val timelineData: List<TimePoint>,
        val recentEvents: List<EventItem>
    ) : DADetailState()
    data class Error(val message: String) : DADetailState()
}
```

### Canvas drawing approach for timeline
```kotlin
Canvas(modifier = Modifier
    .fillMaxWidth()
    .height(180.dp)) {
    // Calculate dimensions based on canvas size
    // Draw background grid
    // Draw hour labels
    // Draw activity bars (total and blocked)
}
```

## Resources
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design 3 Guidelines](https://m3.material.io/)
- [Prototype Screenshot](screenshot_link)
- [Canvas Drawing in Compose](https://developer.android.com/jetpack/compose/graphics/draw/overview)

## Future Enhancements
- Multi-language support for descriptions
- Integration with GitHub for community contributions
- Interactive timeline with zoom functionality
- Detailed statistics with additional metrics

---

*This document captures contextual information and practical details.*
