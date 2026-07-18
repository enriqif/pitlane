---
name: Pitlane Narrative
colors:
  surface: '#131315'
  surface-dim: '#131315'
  surface-bright: '#39393b'
  surface-container-lowest: '#0e0e10'
  surface-container-low: '#1b1b1d'
  surface-container: '#1f1f21'
  surface-container-high: '#2a2a2c'
  surface-container-highest: '#353437'
  on-surface: '#e4e2e4'
  on-surface-variant: '#c0c9c3'
  inverse-surface: '#e4e2e4'
  inverse-on-surface: '#303032'
  outline: '#8a938e'
  outline-variant: '#404945'
  surface-tint: '#9ed1bd'
  primary: '#9ed1bd'
  on-primary: '#00382a'
  primary-container: '#1b4d3e'
  on-primary-container: '#8abda9'
  inverse-primary: '#376757'
  secondary: '#ffb5a0'
  on-secondary: '#5f1500'
  secondary-container: '#d73b00'
  on-secondary-container: '#fffbff'
  tertiary: '#71d7cd'
  on-tertiary: '#003733'
  tertiary-container: '#004e48'
  on-tertiary-container: '#5bc3b9'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#baeed9'
  primary-fixed-dim: '#9ed1bd'
  on-primary-fixed: '#002117'
  on-primary-fixed-variant: '#1d4f40'
  secondary-fixed: '#ffdbd1'
  secondary-fixed-dim: '#ffb5a0'
  on-secondary-fixed: '#3b0900'
  on-secondary-fixed-variant: '#862200'
  tertiary-fixed: '#8ef4e9'
  tertiary-fixed-dim: '#71d7cd'
  on-tertiary-fixed: '#00201d'
  on-tertiary-fixed-variant: '#00504a'
  background: '#131315'
  on-background: '#e4e2e4'
  surface-variant: '#353437'
typography:
  display-lg:
    fontFamily: Montserrat
    fontSize: 57px
    fontWeight: '700'
    lineHeight: 64px
    letterSpacing: -0.25px
  headline-lg:
    fontFamily: Montserrat
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Montserrat
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  title-lg:
    fontFamily: Montserrat
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
  body-lg:
    fontFamily: Montserrat
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Montserrat
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  label-lg:
    fontFamily: Montserrat
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  label-sm:
    fontFamily: Montserrat
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  gutter: 16px
  margin-mobile: 16px
  margin-tablet: 24px
  stack-sm: 4px
  stack-md: 12px
  stack-lg: 24px
---

## Brand & Style

The design system is engineered for the high-stakes environment of vehicle maintenance, drawing inspiration from professional motorsport pit crews. It targets automotive enthusiasts and meticulous vehicle owners who value precision, speed, and reliability. The emotional response is one of **focused control and mechanical confidence**.

The visual style is a refined interpretation of **Material Design 3 (MD3)**, leaning into a **Corporate / Modern** aesthetic with high-energy accents. It utilizes a dark-first approach to reduce glare in garage settings, employing subtle tonal elevation and crisp layouts to organize complex technical data. The interface feels like a digital dashboard: professional, authoritative, yet approachable through soft geometry.

## Colors

The palette is anchored by **Deep Racing Green**, representing stability and mechanical heritage. **Vibrant Orange** serves as a high-visibility functional accent, used exclusively for primary actions and critical alerts to mimic the urgency of a pit stop.

- **Primary**: Deep Racing Green (#1B4D3E) - Used for key branding and selected states.
- **Secondary**: Vibrant Orange (#FF5722) - Reserved for the Floating Action Button (FAB), progress indicators, and "urgent" maintenance alerts.
- **Surface/Background**: In Dark Mode, use Dark Charcoal (#1C1C1E) for the main background and a slightly lighter charcoal (#2C2C2E) for cards to create depth.
- **Functional**: Success states use the Tertiary Teal, while errors remain tied to the Secondary Orange or a standard error red if distinction is required.

## Typography

This design system utilizes **Montserrat** across all levels to maintain a modern, geometric, yet friendly feel. The typeface’s open counters ensure high legibility in low-light environments.

- **Headlines**: Use Semi-Bold (600) or Bold (700) to establish a clear hierarchy, especially for vehicle names and odometer readings.
- **Numbers**: Use Bold weights for "km" values and currency (ARS) to make them stand out as data points.
- **Localization**: All strings must accommodate Spanish (AR) phrasing, which often requires 20-30% more horizontal space than English. Ensure containers are flexible.

## Layout & Spacing

The system follows the **Material Design 3 8dp grid**. Layouts are fluid, relying on a 4-column structure for mobile and an 8-column structure for tablets.

- **Margins**: A standard 16px side margin is enforced for mobile devices to prevent content from hitting the screen edges.
- **Cards**: Spacing between cards in a vertical list should be 12px (stack-md) to maintain a rhythmic, scanned flow.
- **Touch Targets**: All interactive elements (chips, icons) must maintain a minimum 48x48dp touch target, even if the visual asset is smaller.

## Elevation & Depth

Consistent with MD3, depth is expressed through **Tonal Layers** rather than heavy shadows.

- **Level 0**: Background (#1C1C1E).
- **Level 1**: Default Cards (#2C2C2E). No shadow, or a very subtle 1px ambient blur.
- **Level 2**: Active or Pressed states. Use a subtle overlay of the primary color (8% opacity) to indicate height.
- **FAB**: The only element utilizing a distinct shadow (Elevation 3) to emphasize its role as the primary trigger for data entry.

## Shapes

The shape language is **Rounded**, balancing the industrial nature of automotive tracking with a modern app feel.

- **Small Components**: Checkboxes and chips use a 8px (rounded-md) corner radius.
- **Medium Components**: Cards and input fields use a 16px (rounded-lg) corner radius.
- **Large Components**: Bottom sheets and large dashboard containers use a 24px (rounded-xl) radius on top corners.
- **FAB**: Fully rounded (pill-shaped) to distinguish it from the content cards.

## Components

### Bottom Navigation
A 5-item navigation bar following MD3 specs. Active icons use a tonal pill container in Racing Green. Items: *Inicio, Mi Taller, [FAB], Gastos, Perfil*.

### Floating Action Button (FAB)
The "Pit Stop" button. A large, rounded-square or pill-shaped FAB in **Vibrant Orange**. It sits at the bottom right or center-docked in the navigation bar. Icon: `plus` or `wrench`.

### Elevation Cards
Used for vehicle stats (e.g., "Próximo Service"). Cards feature a 16px internal padding. Title in `title-lg`, secondary data in `body-md`. Use the Racing Green for progress bars within the card.

### Choice Chips
Filtering maintenance history (e.g., "Reparación", "Combustible", "Seguro"). Unselected: Outlined with `neutral-400`. Selected: Solid Racing Green with white text.

### Input Fields
Filled text fields with a 1px bottom stroke in the inactive state, becoming a 2px stroke in Racing Green when focused. Labels should always be visible (floating). Error states switch the stroke and helper text to Orange (#FF5722).

### Iconography
Outlined style (2px stroke). Use metaphors like a 'Oil Can' for fluids, 'Wrench' for general service, and 'Gas Station' for fuel logs.