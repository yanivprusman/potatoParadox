import type { Metadata } from "next";
import "./globals.css";
import FeedbackChatMount from "./FeedbackChatMount";

export const metadata: Metadata = {
  title: "potatoParadox",
  description: "Interactive demo of the potato paradox: near 100%, one percentage point hides a huge change in the ratio",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="en">
      <body>{children}
        <FeedbackChatMount />
      </body>
    </html>
  );
}
