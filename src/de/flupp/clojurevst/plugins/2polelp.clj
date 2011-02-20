(ns de.flupp.clojurevst.clj2polelp)

;;Type : LP 2-pole resonant tweaked butterworth
;;References : Posted by daniel_jacob_werner [AT] yaho [DOT] com [DOT] au

;;Notes :
;This algorithm is a modified version of the tweaked butterworth lowpass filter by Patrice Tarrabia posted on musicdsp.org's archives. 
;It calculates the coefficients for a second order IIR filter. The resonance is specified in decibels above the DC gain. 
;It can be made suitable to use as a SoundFont 2.0 filter by scaling the output so the overall gain matches the specification 
;(i.e. if resonance is 6dB then you should scale the output by -3dB). Note that you can replace the sqrt(2) values in the standard 
;butterworth highpass algorithm with my "q =" line of code to get a highpass also. 
;
;How it works: normally q is the constant sqrt(2), and this value controls resonance. At sqrt(2) resonance is 0dB, smaller values 
;increase resonance. By multiplying sqrt(2) by a power ratio we can specify the resonant gain at the cutoff frequency. 
;The resonance power ratio is calculated with a standard formula to convert between decibels and power ratios (the powf statement...).
;Good Luck,
;Daniel Werner
;http://experimentalscene.com/

;Code :
;float c, csq, resonance, q, a0, a1, a2, b1, b2;

;c = 1.0f / (tanf(pi * (cutoff / samplerate)));
;csq = c * c;
;resonance = powf(10.0f, -(resonancedB * 0.1f));
;q = sqrt(2.0f) * resonance;
;a0 = 1.0f / (1.0f + (q * c) + (csq));
;a1 = 2.0f * a0;
;a2 = a0;
;b1 = (2.0f * a0) * (1.0f - csq);
;b2 = a0 * (1.0f - (q * c) + csq);

;;TODO: this is a quite literal conversion from the original algorithm, needs clojurization (use atoms and let special form). 


;; config
(def plugin-config {:plugin-category 1
                    :unique-id "2plp"
                    :product "clj2plp"
                    :num-inputs 2
                    :num-outputs 2
                    :can-process-replacing true
                    :can-mono false
                    :vendor "clojurevst"
                    :can-do ["2in2out", "plugAsChannelInsert", "plugAsSend"]})

;; parameters
(def #^{:parameter-name "Cutoff"
        :parameter-label "hz"}
        p1 0.04)
(def #^{:parameter-name "Max Cutoff"
        :parameter-label "hz"}
        p3 0.81)
(def #^{:parameter-name "Resonance"
        :parameter-label "db"}
        p2 0.81)
(def #^{:parameter-name "Fine Reso"
        :parameter-label "db"}
        p4 0.9)

;;globals
;constants
(def p1filtercoeff 0.0007)
(def pi 3.1415926535897932384626433832795)

;thread-local vars (use thread-local bindings for them?)
(def p1smooth 0.0)
(def c 0.0)
(def csq 0.0)
(def q 0.0)
(def a0 0.0)
(def a1 0.0)
(def a2 0.0)
(def b1 0.0)
(def b2 0.0)

;state maintained across process() calls
(def out11 0.0)
(def out12 0.0)
(def in11 0.0)
(def in12 0.0)
(def out21 0.0)
(def out22 0.0)
(def in21 0.0)
(def in22 0.0)


;; effect
(defn process-replacing [inputs outputs]
  (let [in1 (nth inputs 0)
        in2 (nth inputs 1)
        out1 (nth outputs 0)
        out2 (nth outputs 1)
        samples (count out1)]
    (dotimes [i samples] 
        (when (= (mod i 20) 0) ;audible param change "lag"
          (def p1smooth (+ (* p1filtercoeff p1) (* (- 1.0 p1filtercoeff) p1smooth))) ;parameter smoothing
	        (def c (/ 1.0 (. java.lang.Math (tan (* pi (/ (+ 0.001 (* p1smooth p3)) 2.15))))))
	        (def csq (* c c))
	        (def q (* (. java.lang.Math (sqrt 2.0)) (- 1.0 p2) (- 1.0 p4)))
	        (def a0 (/ 1.0 (+ 1.0 (* q c) csq)))
	        (def a1 (* 2.0 a0))
	        (def a2 a0)
	        (def b1 (* (* 2.0 a0) (- 1.0 csq)))
	        (def b2 (* a0 (+ (- 1.0 (* c q)) csq))))
	      (aset-float out1 i (- (+ (* (nth in1 i) a0) (* in11 a1) (* in12 a2)) (* out11 b1) (* out12 b2)))
	      (aset-float out2 i (- (+ (* (nth in2 i) a0) (* in21 a1) (* in22 a2)) (* out21 b1) (* out22 b2)))
      (def out12 out11) ;cascade
      (def out11 (nth out1 i))
			(def in12 in11)
			(def in11 (nth in1 i))
			(def out22 out21)
			(def out21 (nth out2 i))
			(def in22 in21)
			(def in21 (nth in2 i)))))
