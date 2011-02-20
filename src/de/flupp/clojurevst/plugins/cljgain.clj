(ns de.flupp.clojurevst.cljgain)
 
;; config
(def plugin-config {:plugin-category 1
                    :unique-id "cljG"
                    :product "cljgain"
                    :num-inputs 1
                    :num-outputs 1
                    :can-process-replacing true
                    :can-mono true
                    :vendor "clojurevst"
                    :can-do ["1in1out", "plugAsChannelInsert", "plugAsSend"]
                    :programs ["foo", "bar"] })

;; parameters
(def #^{:parameter-name "Gain"
        :parameter-label ""
        :value-in-programs [0.5, 0.9] }
       pgain 0.5)

(defn get-parameter-display [param]
  (str (* pgain 10)))

(defn get-parameter-label [param]
  (str "bla"))

;; effect processing
(defn process-replacing [inputs outputs]
  (let [input (nth inputs 0)
        samples (count input)
        output (nth outputs 0)]
    (dotimes [i samples]
      (aset-float output i (* pgain (nth input i))))))