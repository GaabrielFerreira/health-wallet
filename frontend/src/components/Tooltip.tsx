import { useLayoutEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";

type TooltipProps = {
  text: string;
};

const TOOLTIP_WIDTH = 224; // w-56
const GAP = 8;
const MARGIN = 8;

export function Tooltip({ text }: TooltipProps) {
  const [open, setOpen] = useState(false);
  const [coords, setCoords] = useState({ top: 0, left: 0, placement: "top" as "top" | "bottom" });
  const buttonRef = useRef<HTMLButtonElement>(null);

  useLayoutEffect(() => {
    if (!open || !buttonRef.current) return;
    const rect = buttonRef.current.getBoundingClientRect();
    const placement = rect.top > 80 ? "top" : "bottom";

    const rawLeft = rect.left + rect.width / 2 - TOOLTIP_WIDTH / 2;
    const left = Math.max(MARGIN, Math.min(rawLeft, window.innerWidth - TOOLTIP_WIDTH - MARGIN));
    const top = placement === "top" ? rect.top - GAP : rect.bottom + GAP;

    setCoords({ top, left, placement });
  }, [open]);

  return (
    <>
      <button
        ref={buttonRef}
        type="button"
        aria-label="Ajuda"
        onClick={() => setOpen((v) => !v)}
        onMouseEnter={() => setOpen(true)}
        onMouseLeave={() => setOpen(false)}
        onBlur={() => setOpen(false)}
        className="ml-1 w-4 h-4 rounded-full bg-gray-200 hover:bg-purple-200 text-gray-600 hover:text-purple-700 text-[10px] font-bold inline-flex items-center justify-center transition cursor-help"
      >
        ?
      </button>

      {open &&
        createPortal(
          <div
            role="tooltip"
            style={{
              position: "fixed",
              top: coords.top,
              left: coords.left,
              width: TOOLTIP_WIDTH,
              transform: coords.placement === "top" ? "translateY(-100%)" : undefined,
            }}
            className="bg-gray-900 text-white text-xs leading-relaxed rounded-lg px-3 py-2 shadow-lg z-[9999] pointer-events-none"
          >
            {text}
          </div>,
          document.body
        )}
    </>
  );
}
