(ns humble-sand.core
  (:require [io.github.humbleui.canvas :as canvas]
            [io.github.humbleui.core :as core]
            [io.github.humbleui.paint :as paint]
            [io.github.humbleui.window :as window]
            [io.github.humbleui.ui :as ui]
            
            [humble-sand.sand :as sand]))

(def *window (atom nil))
(def *app (atom nil))
(def dim 80)

(def paint-rock
  (paint/fill 0xFF7a5a3e))

(def paint-source
  (paint/fill 0xFF79c1e0))

(def paint-sand
  (paint/fill 0xFFFFCC33))

(defn render-place [canvas place x y]
  (when (= :rock place)
    (canvas/draw-rect canvas (core/rect-xywh x y 1 1) paint-rock))
  (when (= :sand place)
    (canvas/draw-rect canvas (core/rect-xywh x y 1 1) paint-sand))
  (when (= :source place)
    (canvas/draw-rect canvas (core/rect-xywh x y 1 1) paint-source)))

(defn paint [ctx canvas size]
  (let [field (min (:width size) (:height size))
        scale (/ field dim)]
    ; center canvas
    (canvas/translate canvas
                      (-> (:width size) (- field) (/ 2))
                      (-> (:height size) (- field) (/ 2)))

    ; scale to fit full width/height but keep square aspect ratio
    (canvas/scale canvas scale scale)

    ; erase background
    (with-open [bg (paint/fill 0xFFFFFFFF)]
      (canvas/draw-rect canvas (core/rect-xywh 0 0 dim dim) bg))
    
    (doseq [[[x y] place] @sand/cave-system]
      (render-place canvas place (- x 450) y))
    
    ;; schedule redraw on next vsync
    (window/request-frame (:window ctx))))

(def app
  (ui/default-theme
   (ui/center
    (ui/canvas
     {:on-paint paint}))))

(reset! *app app)

(ui/start-app!
 (reset! *window
         (ui/window
          {:title "Humble Sand"
           :width 800
           :height 600}
          *app))
 (def caves (sand/setup (slurp "resources/small_example.txt")))
 (send-off sand/dropper sand/sand-falling)
 )
