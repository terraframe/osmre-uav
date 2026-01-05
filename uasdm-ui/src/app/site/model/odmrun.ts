import { ProcessConfig, SiteEntity } from "./management";

export class ODMRun {
	output: string;
	config: ProcessConfig;
	report: SiteEntity;
	runStart: string;
	runEnd: string;
}

export interface RuntimeEstimate {runtime: number, confidence: number, similarJobs: number};
export type RuntimeDisplay = { primary: string; secondary?: string; };

function formatRuntime(seconds: number): string {
    // Round to nearest minute first
    const totalMinutes = Math.round(seconds / 60);

    const d = Math.floor(totalMinutes / (24 * 60));
    const h = Math.floor((totalMinutes % (24 * 60)) / 60);
    const m = totalMinutes % 60;

    const parts: string[] = [];

    if (d > 0) parts.push(`${d}d`);
    if (h > 0) parts.push(`${h}h`);

    // Only show minutes if they add information
    if (m > 0 || parts.length === 0) {
        parts.push(`${m}m`);
    }

    return parts.join(" ");
}

export function getRuntimeDisplay(seconds: number): RuntimeDisplay {
    if (!Number.isFinite(seconds) || seconds < 0) {
        return { primary: "Unknown" };
    }

    // 1) Seconds
    if (seconds < 60) {
        const s = Math.round(seconds);
        return { primary: `${s} second${s === 1 ? "" : "s"}` };
    }

    // 2) Minutes (under 1 hour)
    if (seconds < 3600) {
        const m = Math.round(seconds / 60);
        return { primary: `${m} minute${m === 1 ? "" : "s"}` };
    }

    // 3) Hours (under 1 day)
    if (seconds < 86400) {
        const h = Math.round(seconds / 3600);
        return {
        primary: `${h} hour${h === 1 ? "" : "s"}`,
        secondary: formatRuntime(seconds),
        };
    }

    // 4) Days+
    const d = Math.floor(seconds / 86400); // floor days for a stable label ("2 days" doesn't flip as often)
    return {
        primary: `${d} day${d === 1 ? "" : "s"}`,
        secondary: formatRuntime(seconds),
    };
}
