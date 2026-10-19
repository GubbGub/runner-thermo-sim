# Runner Biomechanics & Thermodynamics Simulator 🏃‍♂️🔥

A modular Java application built to simulate and analyze the mechanical work (Joules) and power output (Watts) of runners during physical exertion. This project bridges biological parameters with classical Newtonian mechanics ($F=ma$) to demonstrate how mass and height alter energy expenditure.

> *"Coding reveals truth / like the ancient gift of fire / nature found in text."*

## 🧬 Physics & System Logic
* **Mass Constraints:** Calculates the base force needed to accelerate and maintain velocity relative to individual runner mass ($m$).
* **Thermodynamic Efficiency:** Incorporates human metabolic efficiency curves ($\approx 25\%$) to calculate true caloric burn vs. net mechanical work.
* **Biometric Scaling:** Estimates stride frequency and mechanical advantages using height-to-stride ratios.

## 🚀 How to Run
Ensure you have Java JDK 8 or higher installed. Compile and run from the root directory:
```bash
javac src/*.java
java -cp src Main
```
