(ns truncate.core)

(defn trnct [filename]
      (spit filename ""))

(defn -main [& args]
      (trnct (first args)))
