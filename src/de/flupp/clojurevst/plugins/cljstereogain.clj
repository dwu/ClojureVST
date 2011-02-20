(ns de.flupp.clojurevst.cljstereogain)
 
;; config
(def plugin-config {:plugin-category 1
                    :unique-id "clsG"
                    :product "cljstereogain"
                    :num-inputs 2
                    :num-outputs 2
                    :can-process-replacing true
                    :can-mono false
                    :vendor "clojurevst"
                    :can-do ["2in2out", "plugAsChannelInsert", "plugAsSend"]
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
  (let [in1 (nth inputs 0)
        in2 (nth inputs 1)
        out1 (nth outputs 0)
        out2 (nth outputs 1)
        samples (count in1)]
    (dotimes [i samples]
      (aset-float out1 i (* pgain (nth in1 i)))
      (aset-float out2 i (* pgain (nth in2 i))))))
