import { useEffect, useRef } from "react";

const ShadowComponent = () => {
  const hostRef = useRef(null);

  useEffect(() => {
    if (hostRef.current && !(hostRef.current as HTMLElement).shadowRoot) {
      const shadowRoot = (hostRef.current as HTMLElement).attachShadow({
        mode: "open",
      });

      // Create nested elements with text
      const outerDiv = document.createElement("div");
      outerDiv.id = "shadowOuterDiv";
      outerDiv.style.padding = "20px";
      outerDiv.style.backgroundColor = "#f0f0f0";

      const spanOuterText = document.createElement("span");
      spanOuterText.id = "spanOuterText";
      spanOuterText.textContent = "This is the outer div in the Shadow DOM";

      const innerDiv = document.createElement("div");
      innerDiv.id = "shadowInnerDiv";
      innerDiv.style.marginTop = "10px";
      innerDiv.style.padding = "10px";
      innerDiv.style.backgroundColor = "#ccc";

      const innerTextSpan = document.createElement("span");
      innerTextSpan.id = "spanInnerText";
      innerTextSpan.textContent = "This is the inner div in the Shadow DOM";

      // Append inner div to outer div
      outerDiv.appendChild(spanOuterText);
      outerDiv.appendChild(innerDiv);
      innerDiv.appendChild(innerTextSpan);

      // Append outer div to the shadow root
      shadowRoot.appendChild(outerDiv);
    }
  }, []);

  return <div ref={hostRef}>Shadow Host Element</div>;
};

export default ShadowComponent;
