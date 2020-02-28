package frc.util;

public class SimpleProp{

    private double kFB, kSP, kPB, kDB, outMn, outMx;

    public SimpleProp(double _kSP, double _kPB, double _kDB,
    /*             */ double _outMn, double _outMx){
        kSP = _kSP;  kPB = _kPB;  kDB = _kDB;
        outMn = _outMn;  outMx = _outMx;
    }

    public SimpleProp(double[] parm ){
        kSP = parm[0];
        kPB = parm[1];
        kDB = parm[2];
        outMn = parm[3];
        outMx = parm[4];
    }

    // Calculate a simple proportional responce.
    public double calcProp(double inFB, boolean prnt){
        double err = inFB - kSP;   //error
        if(prnt) System.out.println("Mn- " + outMn + "  Mx- " + outMx);
        if(Math.abs(err) < kDB || kPB == 0.0) return  0.0; //In deadband or PB is 0
        err /= kPB;  //else calc proportional, neg. else pos.
        if(prnt) System.out.print("err1- " + err);
        err = err < 0 ?
        BotMath.Span(err, -1.0, 0.0, -outMx, -outMn, true, 0) : //Neg.
        BotMath.Span(err, 0.0, 1.0, outMn, outMx, true, 0);     //else Pos.
        if(prnt) System.out.println("  err2- " + err);
        return err;
    }

    // Set k's
    public void setSP(double _kSP) { kSP = _kSP; }
    public void setPB(double _kPB) { kPB = _kPB; }
    public void setDB(double _kDB) { kDB = _kDB; }
    public void setOutMn(double _outMn) { outMn = _outMn; }
    public void setOutMx(double _outMx) { outMx = _outMx; }

    // Get k's
    public double getSP() { return kSP; }
    public double getPB() { return kPB; }
    public double getDB() { return kDB; }
    public double getOutMn() { return outMn; }
    public double getOutMx() { return outMx; }
}
