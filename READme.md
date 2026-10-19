<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="assets/readme/hero-dark.png">
    <img alt="Runner Thermo. Where the energy of a run goes: mechanical work, heat and power, calculated from your own watch data." src="assets/readme/hero-light.png" width="100%">
  </picture>
</p>

<p align="center">
  <a href="#the-story">The story</a> &nbsp;·&nbsp;
  <a href="#by-the-numbers">By the numbers</a> &nbsp;·&nbsp;
  <a href="#the-physics">The physics</a> &nbsp;·&nbsp;
  <a href="#the-app">The app</a>
</p>

<br>

## The story

In AP Physics II we ran a lab on runners. We measured each runner's mass and timed a lap, then worked out how much mechanical work they did and how much power they put out.

I run, and I kept thinking about where all that energy actually goes. So I pointed the same physics at my own watch data.

The answer surprised me. Most of it never moves you at all. Muscles are only about 25 percent efficient, so for every joule that carries you forward, three more turn into heat. That's why you're soaked by the end of a hard 5K.

Runner Thermo turns the lab into a small Java application. It has a terminal version and a Mac app, both built on the same physics engine.

> *Coding reveals truth*  
> *like the ancient gift of fire,*  
> *nature found in text.*

<br>

## By the numbers

A 70 kg runner finishing a 5K in 21:20:

| | |
|:--|:--|
| **Energy burned** | 350 kcal (1.46 MJ) |
| **Mechanical work** | 366 kJ, the part that actually moves you |
| **Released as heat** | 1.10 MJ, three quarters of the total |
| **If none escaped** | Body temperature would rise about 4.5 °C |
| **Mechanical power** | 286 W, about 4 W per kilogram |
| **Steps taken** | About 3,700 |

<br>

## The physics

There are two honest ways to estimate the work in a run. The app calculates both, because the gap between them is the most interesting part.

**From the lab.** Physiologists have measured that running on level ground costs about 1 kilocalorie per kilogram per kilometer, at almost any speed. Multiply by muscle efficiency and you get the mechanical work.

**From Newton.** Treat the runner as a single mass. Every step lifts the body a few centimeters, which takes $mg\Delta h$. Every time you speed up, kinetic energy grows by $\tfrac{1}{2}m(v_2^2 - v_1^2)$. Add those up across the whole run.

The Newton version explains only about half of what the lab measures. The rest goes to swinging your arms and legs and to braking a little on every footstrike, partly paid back by your Achilles tendons, which store energy and return it like springs.

Whatever doesn't become work becomes heat. That's the first law of thermodynamics, and it's why the app can say how warm a run would make you if your body couldn't sweat.

<details>
<summary><b>The equations</b></summary>

<br>

**Cost of transport**

$$E_{\text{metabolic}} = C \cdot m \cdot d, \qquad C \approx 4.18\ \text{J/(kg·m)}$$

$$W = \eta \cdot E_{\text{metabolic}}, \qquad \eta \approx 0.25$$

**Center of mass**

$$W = N_{\text{steps}} \cdot mg\Delta h \;+\; \sum \tfrac{1}{2}m\left(v_i^2 - v_{i-1}^2\right)^{+}$$

**Heat and temperature**

$$Q = E_{\text{metabolic}} - W, \qquad \Delta T = \frac{Q}{m \, c_{\text{body}}}, \quad c_{\text{body}} \approx 3470\ \text{J/(kg·K)}$$

**Stride**

Step length is speed divided by cadence. The Froude number $Fr = v^2 / gL_{\text{leg}}$ compares speed with leg length; above about 0.5, people switch from walking to running.

Every constant lives in [`PhysicsConstants.java`](src/runnersim/physics/PhysicsConstants.java), so each assumption is easy to find and question.

</details>

<br>

## The app

<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="assets/readme/app-dark.png">
    <img alt="The Runner Thermo Mac app showing a 5 km run: 366 kJ of mechanical work, 350 kcal burned, 1.10 MJ of heat and 286 W of mechanical power." src="assets/readme/app-light.png" width="760">
  </picture>
</p>

Type in your height, weight and a run. If you have a watch, add your cadence, vertical oscillation and lap splits for better numbers. Anything you leave out is estimated and labeled that way.

**Built to teach.** Every physics term has a small **?** beside it. Click it to see what the term means in plain English, along with its formula. In the terminal version, type a term like `froude` after your results.

**Run it from source** (Java 17 or newer)

```bash
./build.sh test   # physics tests, checked against hand calculations
./build.sh cli    # terminal version
./build.sh gui    # window version
```

**Build the app**

```bash
./build.sh app
```

This packages `Runner Thermo.app` and installs it in `/Applications`.

<br>

## Roadmap

- [x] **Two models.** Lab measurements and Newton, side by side.
- [x] **Two interfaces.** A terminal version and a Mac app on one engine.
- [x] **Explanations.** A ? on every physics term, from cadence to the Froude number.
- [ ] **Lab comparison.** Enter numbers measured in class and see how close the estimates come.
- [ ] **Watch files.** Import a run from GPX or FIT instead of typing splits.
- [ ] **Hills.** Add the work of climbing, $mgh$.
- [ ] **Wind.** Add air resistance, $\tfrac{1}{2}\rho C_d A v^2$.
- [ ] **Reality check.** Compare my numbers with a treadmill test.

<br>

## Repository

| Path | What it is |
|:--|:--|
| `src/runnersim/model/` | What a runner and a run are |
| `src/runnersim/physics/` | The engine: both work models, stride, and the report |
| `src/runnersim/learn/` | Plain-language explanations behind every ? |
| `src/runnersim/cli/` | The terminal version |
| `src/runnersim/gui/` | The Mac app |
| `test/` | Physics tests |
| `assets/` | App icon and README images |

Each work model is one class that implements `WorkModel`, so adding a new one, like running uphill, doesn't touch anything else.

<br>

## Acknowledgments

- My AP Physics II class, for the lab that started this
- Rodolfo Margaria and colleagues, whose 1963 measurements of the energy cost of running still hold up

<br>

## License

[MIT](LICENSE)
