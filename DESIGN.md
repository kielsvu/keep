# KeepJ Design Contract

## Product
KeepJ is a private password vault. The interface should feel calm, trustworthy, tactile, and finished.

## North star
Apple-quality interaction design without copying Apple branding. KeepJ should feel native to Android while showing the same level of care: clear hierarchy, restrained surfaces, purposeful motion, immediate feedback, and excellent empty/error states.

## Rules
- Every visual element earns its place.
- Prefer hierarchy and whitespace over cards inside cards.
- No decorative gradients, glow, glass, or floating ornaments unless they communicate state.
- Surfaces use a small elevation ladder: background, surface, raised, overlay.
- Corners are restrained: 12–20dp for controls and 16–22dp for major surfaces.
- Primary actions are obvious without being oversized.
- Touch feedback is fast and tactile. Avoid slow generic animations.
- Motion explains state changes; it does not delay the user.
- Enter transitions: 180–360ms. Press feedback: ~100ms. State changes: 120–240ms.
- Use spring motion for direct manipulation and tween motion for navigation/state choreography.
- Do not animate every element independently.
- Error states are clear and recoverable. Never rely on color alone.
- Passwords and sensitive values never appear in logs or transient UI longer than necessary.
- Interactive controls expose useful content descriptions.
- Respect system font scaling, insets, keyboard behavior, and reduced-motion preferences where available.

## Anti-patterns
- Generic SaaS dashboard cards
- Purple/blue gradients
- Giant pill buttons everywhere
- Excessive rounded containers
- Decorative animation on startup
- Long 400–600ms navigation transitions
- Nested cards with no hierarchy
- Tiny secondary text used to create hierarchy
- Toasts for important security state

## Review loop
1. Build or statically validate.
2. Trace every user path and state transition.
3. Check empty, loading, error, keyboard, back, and locked states.
4. Check accessibility labels and touch targets.
5. Remove anything that feels ornamental or generic.
6. Only then call the surface finished.

## Performance contract

KeepJ should stay responsive without flattening its motion language.

- Never perform PBKDF2, vault encryption/decryption, JSON serialization, or backup file I/O on the main dispatcher.
- Prefer one decrypted vault snapshot for search/filter/sort instead of decrypting every query independently.
- Keep list-item work cheap: remember derived display values and avoid whole-list transformations during scroll-driven recomposition.
- Prefer lifecycle-aware state collection for Compose screens.
- Keep interaction motion short and tactile. Performance work must not remove press feedback, navigation depth, or meaningful state transitions just to make the app look faster.
- Avoid continuous/infinite decorative animations in the vault.
- Keep animation work localized to the element changing state.
- Treat release builds with R8/resource shrinking as the production performance baseline.
