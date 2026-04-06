playon = {}
-- UI
function playon.show_text(text)
    mp.set_property("user-data/playon/show_text", text)
end
function playon.hide_ui()
    mp.set_property("user-data/playon/toggle_ui", "hide")
end
function playon.show_ui()
    mp.set_property("user-data/playon/toggle_ui", "show")
end
function playon.toggle_ui()
    mp.set_property("user-data/playon/toggle_ui", "toggle")
end
function playon.show_subtitle_settings()
    mp.set_property("user-data/playon/show_panel", "subtitle_settings")
end
function playon.show_subtitle_delay()
    mp.set_property("user-data/playon/show_panel", "subtitle_delay")
end
function playon.show_audio_delay()
    mp.set_property("user-data/playon/show_panel", "audio_delay")
end
function playon.show_video_filters()
    mp.set_property("user-data/playon/show_panel", "video_filters")
end
function playon.show_software_keyboard()
    mp.set_property("user-data/playon/software_keyboard", "show")
end
function playon.hide_software_keyboard()
    mp.set_property("user-data/playon/software_keyboard", "hide")
end
function playon.toggle_software_keyboard()
    mp.set_property("user-data/playon/software_keyboard", "toggle")
end
-- Custom buttons
function playon.set_button_title(text)
    mp.set_property("user-data/playon/set_button_title", text)
end
function playon.reset_button_title()
    mp.set_property("user-data/playon/reset_button_title", "unused")
end
function playon.hide_button()
    mp.set_property("user-data/playon/toggle_button", "hide")
end
function playon.show_button()
    mp.set_property("user-data/playon/toggle_button", "show")
end
function playon.toggle_button()
    mp.set_property("user-data/playon/toggle_button", "toggle")
end
-- Controls
function playon.previous_episode()
    mp.set_property("user-data/playon/switch_episode", "p")
end
function playon.next_episode()
    mp.set_property("user-data/playon/switch_episode", "n")
end
function playon.pause()
    mp.set_property("user-data/playon/pause", "pause")
end
function playon.unpause()
    mp.set_property("user-data/playon/pause", "unpause")
end
function playon.pauseunpause()
    mp.set_property("user-data/playon/pause", "pauseunpause")
end
function playon.seek_by(value)
    mp.set_property("user-data/playon/seek_by", value)
end
function playon.seek_to(value)
    mp.set_property("user-data/playon/seek_to", value)
end
function playon.seek_by_with_text(value, text)
    mp.set_property("user-data/playon/seek_by_with_text", value .. "|" .. text)
end
function playon.seek_to_with_text(value, text)
    mp.set_property("user-data/playon/seek_to_with_text", value .. "|" .. text)
end
function playon.int_picker(title, name_format, start, stop, step, property)
    mp.set_property("user-data/playon/launch_int_picker", title .. "|" .. name_format ..  "|" .. start .. "|" .. stop .. "|" .. step .. "|" .. property)
end
-- Legacy
function playon.left_seek_by(value)
    playon.seek_by(-value)
end
function playon.right_seek_by(value)
    playon.seek_by(value)
end
return playon
