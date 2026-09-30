package org.firstinspires.ftc.teamcode.StarterBot;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class launcher {
    public final DcMotorEx launcherMotor;
    public final CRServo launcherServo;
    public launcher(HardwareMap hardwareMap) {
        launcherMotor = hardwareMap.get(DcMotorEx.class, "launcher");
        launcherServo = hardwareMap.get(CRServo.class, "launcherServo");
    }
}
