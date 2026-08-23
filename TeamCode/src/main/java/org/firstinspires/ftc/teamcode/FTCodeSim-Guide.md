## Using FTCodeSim

Welcome! This is a guide to **FTCodeSim**, the robot simulator this repo already
uses. It lets you run and test drivetrain/robot code *without a physical robot* —
no field, no hubs, no charged battery required. You can tune drivetrain constants,
try out logic, and watch a virtual robot move around, all from your laptop.

This guide assumes you can already write basic Java and have Android Studio open
on this project. You don't need to touch any Gradle files — the simulator is
already wired into the project (see below).

> Heads up on naming: you may hear this called "FTCCodeSim" around the team, but
> the actual library's classes and imports all start with `FTCodeSim` (one "C").
> That's the name you'll see in code, so that's the name used in this guide.

## What FTCodeSim actually does

FTCodeSim gives you fake versions of FTC hardware (motors, sensors) plus a
physics model for your drivetrain, and drives them from a simulated Driver
Station. Concretely it provides:

- A physics-based **drivetrain simulation** (currently: mecanum) driven by
  motor power, so you can tune real-world constants like max velocity,
  acceleration, and friction and see how the robot actually behaves.
- A simulated **Driver Station window** — OpMode init/start/stop, telemetry.
- **Gamepad input** from your keyboard or a real controller, so you can drive
  the simulated robot around.
- A fake **HardwareMap** (`SimHardwareMap`) so simulated devices can be
  registered the same way real hardware is.

## It's already set up in this repo

Someone already added FTCodeSim to this project, so there's
nothing to configure. For reference, this lives in
[`build.dependencies.gradle`](../../../../../../../../build.dependencies.gradle):

```groovy
repositories {
    // ...
    maven { url = uri("https://repo.dairy.foundation/releases") }
    maven { url = uri("https://repo.dairy.foundation/snapshots") }
}

dependencies {
    // ...
    implementation("org.codeblooded.ftcodesim:ftcodesim:v0.1.0-alpha.1")
}
```

That's a pinned release version (`v0.1.0-alpha.1`), not a moving snapshot — but
it's still an early alpha, so things can change between versions. If FTCodeSim
gets an update later and something stops compiling, check with a mentor before
bumping that version string — don't just change it on your own.

## Where simulator code lives, and how to run it

Simulations run as **plain JUnit tests**, not as real OpModes and not as
Android instrumented tests. They live under:

```
TeamCode/src/test/java/
```

That's a plain-JVM test source set (there's no `androidTest` folder in this
project) — put new simulation classes there, not under `src/main/java`.

To run one:

- **In Android Studio:** open the test class and click the green ▶ gutter icon
  next to the `@Test` method (or the class name to run everything in the file).
- **From the command line:**
  ```bash
  ./gradlew :TeamCode:test
  ```

Running a sim test opens the simulated Driver Station window and starts the
simulation loop.

## Walking through the existing example

There's already a working example at
[`TeamCode/src/test/java/SimulateCodeBloodedDecode.java`](../../../../../../test/java/SimulateCodeBloodedDecode.java).
Open it side-by-side with this section. Run it once before reading further so
you know what you're aiming to understand.

**1. Create the fake hardware map**

```java
SimHardwareMap simHardwareMap = new SimHardwareMap();
```

This stands in for the real FTC `HardwareMap` you'd normally get from an
OpMode — you register simulated devices on it instead of configuring real
ones on the Driver Station.

**2. Describe the drivetrain**

```java
SimMecanumConfig mecanumConfig = new SimMecanumConfig();
mecanumConfig.frontLeftMotorName = "frontLeft";
mecanumConfig.frontRightMotorName = "frontRight";
mecanumConfig.backLeftMotorName = "backLeft";
mecanumConfig.backRightMotorName = "backRight";
mecanumConfig.wheelbase = 9.37008;
mecanumConfig.trackWidth = 9.13386;
mecanumConfig.wheelRadius = 1.889765;
mecanumConfig.staticVelocityRegion = 2;
mecanumConfig.staticFriction = 45;
mecanumConfig.maxAcceleration = 150;
mecanumConfig.maxVelocity = 75;
mecanumConfig.naturalDeceleration = 40;
mecanumConfig.strafeEfficiency = 0.80;
mecanumConfig.robotGeometry = new RobotGeometry(12, 18, 2, 0);
```

These numbers describe the physical robot: motor names (matching what your
real robot config uses), wheel geometry, and how the drivetrain accelerates,
decelerates, and resists motion. These are the values you'll tweak to match
your actual robot's behavior — that's most of what "tuning in sim" means.

**3. Build and register the simulated drivetrain and sensors**

```java
SimulatedDrivetrain drivetrain = new SimulatedMecanum(mecanumConfig);

simHardwareMap.register(drivetrain);
simHardwareMap.register("pinpoint", new SimGobildaPinpoint(drivetrain));
```

The drivetrain gets registered like a piece of hardware. The example also
registers a simulated goBILDA Pinpoint odometry sensor, named `"pinpoint"` —
the same name you'd give it in a real hardware config — that reports position
based on the simulated drivetrain's motion.

**4. Configure the simulation itself**

```java
SimConfig simConfig = new SimConfig();
simConfig.gamepad1Keybinds = new DefaultKeybinds();
simConfig.gamepad2Keybinds = new DefaultKeybinds();
simConfig.simHardwareMap = simHardwareMap;
simConfig.loopTimeMs = 20;
```

`DefaultKeybinds` maps your keyboard to virtual gamepad 1/2 input.
`loopTimeMs` sets how often the simulation loop runs (20ms ≈ a normal FTC
50Hz control loop).

**5. Run it**

```java
FTCodeSim sim = new FTCodeSim(simConfig);
sim.run();
```

This opens the Driver Station window and starts simulating.

## Writing your own sim test

1. Create a new `.java` file under `TeamCode/src/test/java/` (e.g.
   `SimulateMyDrivetrain.java`).
2. Copy the structure from `SimulateCodeBloodedDecode.java` above.
3. Adjust the `SimMecanumConfig` values to match whatever you're testing —
   different wheel geometry, different friction/acceleration numbers, etc.
4. Mark your method `@Test` (from `org.junit.Test`) and run it the same way
   described above.

Small, focused test classes are easier to iterate on than one giant file — if
you're testing something different from the drivetrain physics (like a new
sensor or a scoring mechanism idea), it's fine to make a new test class
rather than growing the existing one.

## Watching the simulation in AdvantageScope

FTCodeSim streams live robot data out over a log server that
[AdvantageScope](https://docs.advantagescope.org/) can visualize (position,
telemetry, etc. over time) — the same tool FRC teams use to debug robot logs.

1. Run your sim test so it's live.
2. Open AdvantageScope.
3. Choose **"Connect to Simulator" → "RLOG Server"**.

You should see live values updating as the simulation runs.

## Driving the simulated robot

Once a sim test is running and the Driver Station window is up, use the
init/start/stop controls in that window like you would on a real Driver
Station. With `DefaultKeybinds`, your keyboard acts as gamepad 1 (and
gamepad 2, per the config above) — drive it around and watch the simulated
drivetrain respond based on the physics constants you set.

## Troubleshooting / things that trip people up

- **`android.util.Log` doesn't work here.** Simulation tests run as plain JVM
  code, not on an Android device, so the real Android `Log` class isn't
  available (its stub throws at runtime in unit tests). This repo already
  includes a substitute at
  [`TeamCode/src/test/java/android/log/Log.java`](../../../../../../test/java/android/log/Log.java)
  — note the package is `android.log`, **not** `android.util`. If you see log
  calls in code you're reading and they don't seem to come from
  `android.util.Log`, this is why.
- **Don't put sim tests in `androidTest`.** This project doesn't have an
  `androidTest` source set at all — everything simulation-related belongs
  under the plain `src/test/java` folder.
- **Sim tests aren't your real OpModes.** Your team's actual competition
  OpModes (e.g.
  [`FieldCentricTeleop.java`](ftcodesim/opmode/FieldCentricTeleop.java))
  extend the normal FTC `OpMode` class and run for real on the robot. The
  simulation harness currently builds its *own* standalone drivetrain model
  in the test file rather than instantiating those OpMode classes directly —
  so changes you make inside a real OpMode won't automatically show up in the
  simulator. Keeping the two in sync (or wiring the sim to run your actual
  OpMode code) is a great thing to explore once you're comfortable with the
  basics above.

## Where to look next

- The simulator classes used in the example all live under
  `org.codeblooded.ftcodesim.*` (e.g. `simulator.FTCodeSim`,
  `hardware.drivetrain.*`, `hardware.devices.*`, `input.DefaultKeybinds`) —
  since this is a compiled dependency, the easiest way to explore its full
  API is to hold Cmd/Ctrl and click into a class from
  `SimulateCodeBloodedDecode.java` in Android Studio.
- Your team's own robot code that this simulator is meant to help test lives
  under
  [`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/ftcodesim/`](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/ftcodesim/)
  — `drivetrain/`, `opmode/`, and `utils/` are good folders to read through
  next.
- If you get stuck, ask a mentor or a teammate who's used it before — and
  once you figure something out that wasn't obvious from this guide, add it
  here for the next person.
