(ns de.flupp.clojurevst.cljdelay)

;; config
(def plugin-config {:plugin-category 1
                    :unique-id "cljD"
                    :product "cljdelay"
                    :num-inputs 1
                    :num-outputs 1
                    :can-process-replacing true
                    :can-mono true
                    :vendor "clojurevst"
                    :can-do ["1in1out", "plugAsChannelInsert", "plugAsSend"]})

;; parameters
(def #^{:parameter-name "Delay"
        :parameter-label ""
        :parameter-display-multiplier 44099}
       pdelay 0.5)
(def #^{:parameter-name "Feedback"
        :parameter-label ""}
       pfeedback 0.9)
(def #^{:parameter-name "Out"
        :parameter-label ""}
        pout 0.5)

(def cursor (atom 0))
(def buffer (make-array Float/TYPE 44100))

;; effect
(defn process-replacing [inputs outputs]
  (let [input (nth inputs 0)
        samples (count input)
        output (nth outputs 0)]
    (dotimes [i samples]
      (aset-float output i (* pout (nth buffer @cursor)))
      (aset-float buffer @cursor (+ (nth input i) (* (nth output i) pfeedback)))
      (swap! cursor inc)
      (if (>= @cursor (* pdelay 44099))
        (reset! cursor 0)))))