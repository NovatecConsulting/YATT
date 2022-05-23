import {useEffect, useRef, useState} from "react";

export function useHighlighting(property: any) {
    const [highlightingColor, setHighlightingColor] = useState("none")
    const previousProp = useRef(property).current
    useEffect(() => {
        if (previousProp !== property) {
            setHighlightingColor("AnimateHighlighting 2s");
            const timer = setTimeout(() => {
                setHighlightingColor("none");
            }, 2000); // The duration of the animation defined in the CSS file

            // Clear the timer before setting a new one
            return () => {
                clearTimeout(timer);
            };
        }
    }, [property, previousProp]);
    return highlightingColor
}