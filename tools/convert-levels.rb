#!/usr/bin/ruby

require 'fileutils'

ACTION_CLOSE = 1
ACTION_OPEN = 2
ACTION_REQ_KEY = 3
ACTION_WALL = 4
ACTION_NEXT_LEVEL = 5
ACTION_NEXT_TUTOR_LEVEL = 6
ACTION_DISABLE_PISTOL = 7
ACTION_ENABLE_PISTOL = 8
ACTION_WEAPON_HAND = 9
ACTION_RESTORE_HEALTH = 10
ACTION_SECRET = 11
ACTION_UNMARK = 12
ACTION_ENSURE_WEAPON = 13
ACTION_BTN_ON = 14
ACTION_BTN_OFF = 15
ACTION_MSG_ON = 16
ACTION_MSG_OFF = 17
ACTION_SELECT_CONTROLS = 18

BUTTONS_MAP = {
	'FORWARD' => 1,
	'BACKWARD' => 2,
	'STRAFE_LEFT' => 3,
	'STRAFE_RIGHT' => 4,
	'ACTION' => 5,
	'NEXT_WEAPON' => 6,
	'ROTATE_LEFT' => 7,
	'ROTATE_RIGHT' => 8,
	'TOGGLE_MAP' => 9,
	'STRAFE_MODE' => 10,
}

MESSAGES_MAP = {
	'PRESS_FORWARD' => 1,
	'PRESS_ROTATE' => 2,
	'PRESS_ACTION_TO_OPEN_DOOR' => 3,
	'SWITCH_AT_RIGHT' => 4,
	'PRESS_ACTION_TO_SWITCH' => 5,
	'KEY_AT_LEFT' => 6,
	'PRESS_ACTION_TO_FIGHT' => 7,
	'PRESS_MAP' => 8,
	'PRESS_NEXT_WEAPON' => 9,
	'OPEN_DOOR_USING_KEY' => 10,
	'PRESS_END_LEVEL_SWITCH' => 11,
	'GO_TO_DOOR' => 12,
}

def convert_level(from_name, actions_name, to_name)
	cont = File.open(from_name, 'rb'){ |fi| fi.read }
	data = cont.gsub(/[^0-9A-Za-z]/, '').scan(/../).map { |v| v.to_i(16) }

	h = data[0]
	w = data[1]

	pos = 4
	level = []

	for i in 1 .. h
		line = []

		for j in 1 .. w
			line << data[pos]
			pos += 1
		end

		level << line
	end

	marks = []
	avail_marks = {}

	for i in 1 .. h
		for j in 1 .. w
			mark = data[pos]
			pos += 1

			unless mark == 0
				avail_marks[mark] = true
				marks << { :mark => mark, :x => j-1, :y => i-1 }
			end
		end
	end

	for i in 1 ... h-1
		for j in 1 ... w-1
			idx = level[i][j]
			vert = (0x10 ... 0x50).include?(level[i-1][j]) && (0x10 ... 0x50).include?(level[i+1][j])

			if (0x50 ... 0x70).include?(idx)		# update door hor/vert state
				idx = 0x50 + (idx % 0x10) + (vert ? 0x10 : 0)
			elsif (0x40 ... 0x50).include?(idx)		# update transparents (not all) hor/vert state
				idx = 0x40 + (idx % 8) + (vert ? 8 : 0)
			end

			level[i][j] = idx
		end
	end

	used_marks = {}
	actions = {}

	if File.exists?(actions_name)
		File.open(actions_name, 'rb') do |fi|
			fi.each_line do |line|
				line = line.strip
				next if line.empty?

				spl = line.split(':').map{ |v| v.strip }.reject{ |v| v.empty? }
				key = (spl.first == '' ? 0 : spl.first.to_i)
				values = spl.last.split(' ').map{ |v| v.strip }.reject{ |v| v.empty? }

				if key > 0 && key < 100 && !avail_marks.key?(key)
					puts "Unknown mark #{key}"
					next
				end

				used_marks[key] = true unless key == 0
				actions[key] = [] unless actions.key?(key)

				pos = 0

				while pos < values.size
					val = values[pos]
					pos += 1

					if ['close', 'open', 'unmark'].include?(val)
						mark = values[pos].to_i
						pos += 1

						if avail_marks.key?(mark)
							used_marks[mark] = true
							actions[key] << (val == 'unmark' ? ACTION_UNMARK : (val == 'close' ? ACTION_CLOSE : ACTION_OPEN))
							actions[key] << mark
						end
					elsif ['req_key', 'wall'].include?(val)
						mark = values[pos].to_i
						param = values[pos + 1].to_i
						pos += 2

						if avail_marks.key?(mark)
							used_marks[mark] = true
							actions[key] << (val == 'req_key' ? ACTION_REQ_KEY : ACTION_WALL)
							actions[key] << mark
							actions[key] << param
						end
					elsif val == 'next_level'
						actions[key] << ACTION_NEXT_LEVEL
					elsif val == 'next_tutor_level'
						actions[key] << ACTION_NEXT_TUTOR_LEVEL
					elsif val == 'disable_pistol'
						actions[key] << ACTION_DISABLE_PISTOL
					elsif val == 'enable_pistol'
						actions[key] << ACTION_ENABLE_PISTOL
					elsif val == 'weapon_hand'
						actions[key] << ACTION_WEAPON_HAND
					elsif val == 'restore_health'
						actions[key] << ACTION_RESTORE_HEALTH
					elsif ['secret', 'ensure_weapon'].include?(val)
						param = values[pos].to_i
						pos += 1

						actions[key] << (val == 'secret' ? ACTION_SECRET : ACTION_ENSURE_WEAPON)
						actions[key] << param
					elsif val == 'btn_on'
						param = values[pos]
						pos += 1

						if BUTTONS_MAP.key?(param)
							param = BUTTONS_MAP[param]
						else
							puts "Unknown button name \"#{param}\""
							param = 0
						end

						actions[key] << ACTION_BTN_ON
						actions[key] << param
					elsif val == 'btn_off'
						actions[key] << ACTION_BTN_OFF
					elsif val == 'msg_on'
						param = values[pos]
						pos += 1

						if MESSAGES_MAP.key?(param)
							param = MESSAGES_MAP[param]
						else
							puts "Unknown message \"#{param}\""
							param = 0
						end

						actions[key] << ACTION_MSG_ON
						actions[key] << param
					elsif val == 'msg_off'
						actions[key] << ACTION_MSG_OFF
					elsif val == 'select_controls'
						actions[key] << ACTION_SELECT_CONTROLS
					else
						puts "Unknown action \"#{val}\""
					end
				end
			end
		end
	end

	res = [h, w]

	for i in 0 ... h
		for j in 0 ... w
			res << level[i][j]
		end
	end

	marks.each do |item|
		next unless used_marks.key?(item[:mark])

		res << item[:mark]
		res << item[:y]
		res << item[:x]
	end

	res << 255

	if actions.key?(0)
		res << 0
		res += actions[0]
		res << 0
	end

	actions.each do |key, val|
		next if key == 0

		res << key
		res += val
		res << 0
	end

	res << 255

	File.open(to_name, 'wb') { |fo| fo << res.map{ |v| v.chr }.join }
end

def process(preset)
	level_assets_dir = File.dirname(__FILE__) + "/level-assets/#{preset}"
	build_assets_dir = File.dirname(__FILE__) + "/../src/#{preset}/assets"

	FileUtils.mkdir_p(build_assets_dir)

	Dir.open(level_assets_dir).each do |dname|
		next if dname =~ /^\./

		src_dir = "#{level_assets_dir}/#{dname}"
		next unless File.directory?(src_dir)

		dest_dir = "#{build_assets_dir}/#{dname}"
		FileUtils.mkdir_p(dest_dir)

		if dname == 'levels'
			Dir.open(src_dir).each do |name|
				next if name =~ /^\./
				next unless name =~ /^level\-\d+\.txt$/

				actions_name = name.gsub(/level/, 'actions')
				res_name = name.gsub(/\.txt$/, '.map')

				convert_level("#{src_dir}/#{name}", "#{src_dir}/#{actions_name}", "#{dest_dir}/#{res_name}")
				puts "[#{preset}] #{name} converted"
			end
		else
			Dir.open(src_dir).each do |name|
				next if name =~ /^\./

				src_path = "#{src_dir}/#{name}"
				next if File.directory?(src_path)

				FileUtils.copy(src_path, "#{dest_dir}/#{name}")
			end

			puts "[#{preset}] #{dname} copied"
		end
	end
end

process('normal')
process('hardcore')
