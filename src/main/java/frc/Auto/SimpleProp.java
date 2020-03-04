package frc.Auto;

public class SimpleProp {

    private double kSP, kPB, kDB, outMn, outMx, inMn, inMx;
    double tmp;
    double outDelta;
    public SimpleProp(double _kSP, double _kPB, double _kDB, double _outMn, double _outMx, double inMn, double inMx) {
        this.kSP = _kSP;
        this.kPB = _kPB;
        this.kDB = _kDB;
        this.outMn = _outMn;
        this.outMx = _outMx;
        outDelta = ( outMn - outMx );
    }

    public SimpleProp(double[] parm) {
        kSP = parm[0];
        kPB = parm[1];
        kDB = parm[2];
        outMn = parm[3];
        outMx = parm[4];
        inMn = parm[5];
        inMx = parm[6];
        outDelta = ( outMx - outMn );
    }

    // Calculate a simple proportional responce.
    public double calcProp(double inFB) {
        double err = kSP - inFB; // error

        if (Math.abs(err) < kDB || kPB == 0.0)
            return 0.0; // In deadband or PB is 0

        err /= kPB; // calc proportional

        
        //span
        tmp = (err - inMn) / (inMx - inMn);
        tmp = (tmp * outDelta) + outMn;



        return tmp;
    }

    // Set k's
    public void setSP(double _kSP) {
        kSP = _kSP;
    }

    public void setPB(double _kPB) {
        kPB = _kPB;
    }

    public void setDB(double _kDB) {
        kDB = _kDB;
    }

    public void setOutMn(double _outMn) {
        outMn = _outMn;
    }

    public void setOutMx(double _outMx) {
        outMx = _outMx;
    }

    // Get k's
    public double getSP() {
        return kSP;
    }

    public double getPB() {
        return kPB;
    }

    public double getDB() {
        return kDB;
    }

    public double getOutMn() {
        return outMn;
    }

    public double getOutMx() {
        return outMx;
    }
}