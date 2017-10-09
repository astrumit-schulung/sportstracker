package de.saring.exerciseviewer.parser.impl.timexPwx;

public class SummaryData {
        private double beginning = 0;
        private double duration = 0;
        private int work = 0;
        private MinMaxAvg hr;
        //        private double durationStopped = 0;
//        private float tss = 0;
//        private int normalizedPower = 0;
        private MinMaxAvg speed;
        //        private MinMaxAvg power;
//        private MinMaxAvg torque;
//        private MinMaxAvg cadence;
        private float distance = 0;
        private MinMaxAvg altitude;
//        private MinMaxAvg temperature;
//        private int variabilityIndex = 0;
//        private float climbingElevation = 0;

        public void setBeginning(double in) {
            beginning = in;
        }

        public double getBeginning() {
            return beginning;
        }

        public void setDuration(double in) {
            duration = in;
        }

        public double getDuration() {
            return duration;
        }

        public void setWork(int in) {
            work = in;
        }

        public int getWork() {
            return work;
        }

        public void setHr(MinMaxAvg in) {
            hr = in;
        }

        public MinMaxAvg getHr() {
            return hr;
        }

        //        public void setDurationStopped(double in){ durationStopped = in; }
//        public double getDurationStopped(){ return durationStopped; }
//        public void setTss(float in){ tss = in; }
//        public float getTss(){ return tss; }
//        public void setNormalizedPower(int in){ normalizedPower = in; }
//        public int getNormalizedPower(){ return normalizedPower; }
        public void setSpeed(MinMaxAvg in) {
            speed = in;
        }

        public MinMaxAvg getSpeed() {
            return speed;
        }

        //        public void setPower(MinMaxAvg in){ power = in; }
//        public MinMaxAvg getPower(){ return power ; }
//        public void setTorque(MinMaxAvg in){ torque = in; }
//        public MinMaxAvg getTorque(){ return torque; }
//        public void setCadence(MinMaxAvg in){ cadence = in; }
//        public MinMaxAvg getCadence(){ return cadence; }
        public void setDistance(float in) {
            distance = in;
        }

        public float getDistance() {
            return distance;
        }

        public void setAltitude(MinMaxAvg in) {
            altitude = in;
        }

        public MinMaxAvg getAltitude() {
            return altitude;
        }
//        public void setTemperature(MinMaxAvg in){ temperature  = in; }
//        public MinMaxAvg getTemperature(){ return temperature; }
//        public void setVariabilityIndex(int in){ variabilityIndex = in; }
//        public int getVariabilityIndex(){ return variabilityIndex; }
//        public void setClimbingElevation(float in){ climbingElevation = in; }
//        public float getClimbingElevation(){ return climbingElevation; }
    }