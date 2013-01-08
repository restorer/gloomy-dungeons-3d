#!/usr/bin/ruby

require 'rubygems'
require 'RMagick'

BASE_ICONS = 0x00
BASE_WALLS = 0x10
BASE_TRANSPARENTS = 0x30
BASE_DOORS_F = 0x50
BASE_DOORS_S = 0x60
BASE_OBJECTS = 0x70
BASE_DECORATIONS = 0x80
BASE_ADDITIONAL = 0x90

COUNT_MONSTER = 0x10	# block = [up, rt, dn, lt], monster = block[walk_a, walk_b, hit], die[3], shoot

# 225 textures max

class TexMapCreator
	def load_texture_mon(result, name, tex)
		xpos = (tex % 8) * 128
		ypos = (tex / 8).floor * 128

		puts name
		img = Magick::ImageList.new(name)
		img.set_channel_depth(Magick::AllChannels, 8)

		unless img.alpha?
			img.alpha(Magick::ResetAlphaChannel)
		end

		result.composite!(img, xpos, ypos, Magick::CopyCompositeOp)
	end

	def load_texture(result, name, tex, flip=false)
		xpos = (tex % 15) * 66
		ypos = (tex / 15).floor * 66

		puts name
		img = Magick::ImageList.new(name)
		img.set_channel_depth(Magick::AllChannels, 8)

		unless img.alpha?
			img.alpha(Magick::ResetAlphaChannel)
		end

		result.composite!(img, xpos + 1, ypos + 1, Magick::CopyCompositeOp)

		if flip
			result.view(xpos, ypos, 66, 66) do |res_view|
				img.view(0, 0, 64, 64) do |img_view|
					for i in 0 .. 63
						res_view[0][i + 1] = img_view[0][i]
						res_view[i + 1][65] = img_view[i][0]
						res_view[65][i + 1] = img_view[63][i]
						res_view[i + 1][0] = img_view[i][63]
					end

					res_view[0][65] = img_view[0][0]
					res_view[0][0] = img_view[0][63]
					res_view[65][65] = img_view[63][0]
					res_view[65][0] = img_view[63][63]
				end
			end
		else
			result.view(xpos, ypos, 66, 66) do |res_view|
				img.view(0, 0, 64, 64) do |img_view|
					for i in 0 .. 63
						res_view[0][i + 1] = img_view[0][i]
						res_view[i + 1][0] = img_view[i][0]
						res_view[65][i + 1] = img_view[63][i]
						res_view[i + 1][65] = img_view[i][63]
					end

					res_view[0][0] = img_view[0][0]
					res_view[0][65] = img_view[0][63]
					res_view[65][0] = img_view[63][0]
					res_view[65][65] = img_view[63][63]
				end
			end
		end
	end

	def process
		base = File.dirname(__FILE__) + '/../level-editor'
		base_icons = File.dirname(__FILE__) + '/../data/icons'
		result = Magick::Image.new(1024, 1024).matte_reset!

		load_texture(result, "#{base}/walls/wall_01.png", BASE_WALLS + 0, true)
		load_texture(result, "#{base}/walls/wall_02.png", BASE_WALLS + 1)
		load_texture(result, "#{base}/walls/wall_03.png", BASE_WALLS + 2)
		load_texture(result, "#{base}/walls/wall_04.png", BASE_WALLS + 3)
		load_texture(result, "#{base}/walls/wall_05.png", BASE_WALLS + 4)
		load_texture(result, "#{base}/walls/wall_06.png", BASE_WALLS + 5, true)
		load_texture(result, "#{base}/walls/wall_07.png", BASE_WALLS + 6, true)
		load_texture(result, "#{base}/walls/wall_08.png", BASE_WALLS + 7, true)
		load_texture(result, "#{base}/walls/wall_09.png", BASE_WALLS + 8, true)
		load_texture(result, "#{base}/walls/wall_10.png", BASE_WALLS + 9, true)
		load_texture(result, "#{base}/walls/wall_11.png", BASE_WALLS + 10, true)
		load_texture(result, "#{base}/walls/wall_12.png", BASE_WALLS + 11, true)
		load_texture(result, "#{base}/walls/wall_13.png", BASE_WALLS + 12)
		load_texture(result, "#{base}/walls/wall_14.png", BASE_WALLS + 13)
		load_texture(result, "#{base}/walls/wall_15.png", BASE_WALLS + 14)
		load_texture(result, "#{base}/walls/wall_16.png", BASE_WALLS + 15)
		load_texture(result, "#{base}/walls/wall_17.png", BASE_WALLS + 16)
		load_texture(result, "#{base}/walls/wall_18.png", BASE_WALLS + 17)
		load_texture(result, "#{base}/walls/wall_19.png", BASE_WALLS + 18)
		load_texture(result, "#{base}/walls/wall_20.png", BASE_WALLS + 19, true)
		load_texture(result, "#{base}/walls/wall_21.png", BASE_WALLS + 20, true)
		load_texture(result, "#{base}/walls/wall_22.png", BASE_WALLS + 21, true)
		load_texture(result, "#{base}/walls/wall_23.png", BASE_WALLS + 22, true)
		load_texture(result, "#{base}/walls/wall_24.png", BASE_WALLS + 23, true)
		load_texture(result, "#{base}/walls/wall_25.png", BASE_WALLS + 24, true)
		load_texture(result, "#{base}/walls/wall_26.png", BASE_WALLS + 25)
		load_texture(result, "#{base}/walls/wall_27.png", BASE_WALLS + 26)
		load_texture(result, "#{base}/walls/wall_28.png", BASE_WALLS + 27)
		load_texture(result, "#{base}/walls/wall_29.png", BASE_WALLS + 28, true)
		load_texture(result, "#{base}/walls/wall_30.png", BASE_WALLS + 29, true)
		load_texture(result, "#{base}/walls/wall_31.png", BASE_WALLS + 30, true)

		load_texture(result, "#{base}/trans/trans_01.png", BASE_TRANSPARENTS + 0)
		load_texture(result, "#{base}/trans/trans_02.png", BASE_TRANSPARENTS + 1, true)
		load_texture(result, "#{base}/trans/trans_03.png", BASE_TRANSPARENTS + 2)
		load_texture(result, "#{base}/trans/trans_04.png", BASE_TRANSPARENTS + 3, true)
		load_texture(result, "#{base}/trans/trans_09.png", BASE_TRANSPARENTS + 8, true)
		load_texture(result, "#{base}/trans/trans_10.png", BASE_TRANSPARENTS + 9)
		load_texture(result, "#{base}/trans/trans_11.png", BASE_TRANSPARENTS + 10)
		load_texture(result, "#{base}/trans/trans_12.png", BASE_TRANSPARENTS + 11)
		load_texture(result, "#{base}/trans/trans_13.png", BASE_TRANSPARENTS + 12)
		load_texture(result, "#{base}/trans/trans_14.png", BASE_TRANSPARENTS + 13)
		load_texture(result, "#{base}/trans/trans_17.png", BASE_TRANSPARENTS + 16, true)
		load_texture(result, "#{base}/trans/trans_18.png", BASE_TRANSPARENTS + 17, true)
		load_texture(result, "#{base}/trans/trans_19.png", BASE_TRANSPARENTS + 18, true)
		load_texture(result, "#{base}/trans/trans_20.png", BASE_TRANSPARENTS + 19, true)

		load_texture(result, "#{base}/doors/door_1_f.png", BASE_DOORS_F + 0)
		load_texture(result, "#{base}/doors/door_2_f.png", BASE_DOORS_F + 1)
		load_texture(result, "#{base}/doors/door_3_f.png", BASE_DOORS_F + 2)
		load_texture(result, "#{base}/doors/door_4_f.png", BASE_DOORS_F + 3)
		load_texture(result, "#{base}/doors/door_5_f.png", BASE_DOORS_F + 4)
		load_texture(result, "#{base}/doors/door_1_s.png", BASE_DOORS_S + 0)
		load_texture(result, "#{base}/doors/door_2_s.png", BASE_DOORS_S + 1)
		load_texture(result, "#{base}/doors/door_3_s.png", BASE_DOORS_S + 2)
		load_texture(result, "#{base}/doors/door_4_s.png", BASE_DOORS_S + 3)
		load_texture(result, "#{base}/doors/door_5_s.png", BASE_DOORS_S + 4)

		load_texture(result, "#{base}/objects/obj_01.png", BASE_OBJECTS + 0)
		load_texture(result, "#{base}/objects/obj_02.png", BASE_OBJECTS + 1)
		load_texture(result, "#{base}/objects/obj_03.png", BASE_OBJECTS + 2)
		load_texture(result, "#{base}/objects/obj_04.png", BASE_OBJECTS + 3)
		load_texture(result, "#{base}/objects/obj_05.png", BASE_OBJECTS + 4)
		load_texture(result, "#{base}/objects/obj_06.png", BASE_OBJECTS + 5)
		load_texture(result, "#{base}/objects/obj_07.png", BASE_OBJECTS + 6)
		load_texture(result, "#{base}/objects/obj_08.png", BASE_OBJECTS + 7)
		load_texture(result, "#{base}/objects/obj_09.png", BASE_OBJECTS + 8)
		load_texture(result, "#{base}/objects/obj_10.png", BASE_OBJECTS + 9)
		load_texture(result, "#{base}/objects/obj_11.png", BASE_OBJECTS + 10)
		load_texture(result, "#{base}/objects/obj_12.png", BASE_OBJECTS + 11)
		load_texture(result, "#{base}/objects/obj_13.png", BASE_OBJECTS + 12)
		load_texture(result, "#{base}/objects/obj_14.png", BASE_OBJECTS + 13)
		load_texture(result, "#{base}/objects/obj_15.png", BASE_OBJECTS + 14)
		load_texture(result, "#{base}/objects/obj_16.png", BASE_OBJECTS + 15)

		load_texture(result, "#{base}/decor/decor_01.png", BASE_DECORATIONS + 0)
		load_texture(result, "#{base}/decor/decor_02.png", BASE_DECORATIONS + 1)
		load_texture(result, "#{base}/decor/decor_03.png", BASE_DECORATIONS + 2)
		load_texture(result, "#{base}/decor/decor_04.png", BASE_DECORATIONS + 3)
		load_texture(result, "#{base}/decor/decor_05.png", BASE_DECORATIONS + 4)
		load_texture(result, "#{base}/decor/decor_06.png", BASE_DECORATIONS + 5)
		load_texture(result, "#{base}/decor/decor_07.png", BASE_DECORATIONS + 6)
		load_texture(result, "#{base}/decor/decor_08.png", BASE_DECORATIONS + 7)
		load_texture(result, "#{base}/decor/decor_09.png", BASE_DECORATIONS + 8)
		load_texture(result, "#{base}/decor/decor_10.png", BASE_DECORATIONS + 9)
		load_texture(result, "#{base}/decor/decor_13.png", BASE_DECORATIONS + 12)

		load_texture(result, "#{base_icons}/icon_up.png", BASE_ICONS + 0)
		load_texture(result, "#{base_icons}/icon_down.png", BASE_ICONS + 1)
		load_texture(result, "#{base_icons}/icon_left.png", BASE_ICONS + 2)
		load_texture(result, "#{base_icons}/icon_right.png", BASE_ICONS + 3)
		load_texture(result, "#{base_icons}/icon_shoot.png", BASE_ICONS + 4)
		load_texture(result, "#{base_icons}/icon_weapon.png", BASE_ICONS + 5)
		load_texture(result, "#{base_icons}/icon_rotate_left.png", BASE_ICONS + 6)
		load_texture(result, "#{base_icons}/icon_rotate_right.png", BASE_ICONS + 7)
		load_texture(result, "#{base_icons}/icon_health.png", BASE_ICONS + 8)
		load_texture(result, "#{base_icons}/icon_armor.png", BASE_ICONS + 9)
		load_texture(result, "#{base_icons}/icon_ammo.png", BASE_ICONS + 10)
		load_texture(result, "#{base_icons}/icon_blue_key.png", BASE_ICONS + 11)
		load_texture(result, "#{base_icons}/icon_red_key.png", BASE_ICONS + 12)
		load_texture(result, "#{base_icons}/icon_green_key.png", BASE_ICONS + 13)
		load_texture(result, "#{base_icons}/icon_map.png", BASE_ICONS + 14)

		load_texture(result, "#{base_icons}/icon_joy.png", BASE_ADDITIONAL + 0)

		result.write(File.dirname(__FILE__) + '/../../source/res/drawable-nodpi/texmap.png')
	end

	def process_mon
		base = File.dirname(__FILE__) + '/../level-editor'
		result = Magick::Image.new(1024, 1024).matte_reset!

		load_texture_mon(result, "#{base}/monsters/mon_1_a1.png", COUNT_MONSTER * 0 + 0)
		load_texture_mon(result, "#{base}/monsters/mon_1_a2.png", COUNT_MONSTER * 0 + 1)
		load_texture_mon(result, "#{base}/monsters/mon_1_a3.png", COUNT_MONSTER * 0 + 2)
		load_texture_mon(result, "#{base}/monsters/mon_1_a4.png", COUNT_MONSTER * 0 + 3)
		load_texture_mon(result, "#{base}/monsters/mon_1_b1.png", COUNT_MONSTER * 0 + 4)
		load_texture_mon(result, "#{base}/monsters/mon_1_b2.png", COUNT_MONSTER * 0 + 5)
		load_texture_mon(result, "#{base}/monsters/mon_1_b3.png", COUNT_MONSTER * 0 + 6)
		load_texture_mon(result, "#{base}/monsters/mon_1_b4.png", COUNT_MONSTER * 0 + 7)
		load_texture_mon(result, "#{base}/monsters/mon_1_c1.png", COUNT_MONSTER * 0 + 8)
		load_texture_mon(result, "#{base}/monsters/mon_1_c2.png", COUNT_MONSTER * 0 + 9)
		load_texture_mon(result, "#{base}/monsters/mon_1_c3.png", COUNT_MONSTER * 0 + 10)
		load_texture_mon(result, "#{base}/monsters/mon_1_c4.png", COUNT_MONSTER * 0 + 11)
		load_texture_mon(result, "#{base}/monsters/mon_1_d1.png", COUNT_MONSTER * 0 + 12)
		load_texture_mon(result, "#{base}/monsters/mon_1_d2.png", COUNT_MONSTER * 0 + 13)
		load_texture_mon(result, "#{base}/monsters/mon_1_d3.png", COUNT_MONSTER * 0 + 14)
		load_texture_mon(result, "#{base}/monsters/mon_1_e.png",  COUNT_MONSTER * 0 + 15)

		load_texture_mon(result, "#{base}/monsters/mon_2_a1.png", COUNT_MONSTER * 1 + 0)
		load_texture_mon(result, "#{base}/monsters/mon_2_a2.png", COUNT_MONSTER * 1 + 1)
		load_texture_mon(result, "#{base}/monsters/mon_2_a3.png", COUNT_MONSTER * 1 + 2)
		load_texture_mon(result, "#{base}/monsters/mon_2_a4.png", COUNT_MONSTER * 1 + 3)
		load_texture_mon(result, "#{base}/monsters/mon_2_b1.png", COUNT_MONSTER * 1 + 4)
		load_texture_mon(result, "#{base}/monsters/mon_2_b2.png", COUNT_MONSTER * 1 + 5)
		load_texture_mon(result, "#{base}/monsters/mon_2_b3.png", COUNT_MONSTER * 1 + 6)
		load_texture_mon(result, "#{base}/monsters/mon_2_b4.png", COUNT_MONSTER * 1 + 7)
		load_texture_mon(result, "#{base}/monsters/mon_2_c1.png", COUNT_MONSTER * 1 + 8)
		load_texture_mon(result, "#{base}/monsters/mon_2_c2.png", COUNT_MONSTER * 1 + 9)
		load_texture_mon(result, "#{base}/monsters/mon_2_c3.png", COUNT_MONSTER * 1 + 10)
		load_texture_mon(result, "#{base}/monsters/mon_2_c4.png", COUNT_MONSTER * 1 + 11)
		load_texture_mon(result, "#{base}/monsters/mon_2_d1.png", COUNT_MONSTER * 1 + 12)
		load_texture_mon(result, "#{base}/monsters/mon_2_d2.png", COUNT_MONSTER * 1 + 13)
		load_texture_mon(result, "#{base}/monsters/mon_2_d3.png", COUNT_MONSTER * 1 + 14)
		load_texture_mon(result, "#{base}/monsters/mon_2_e.png",  COUNT_MONSTER * 1 + 15)

		load_texture_mon(result, "#{base}/monsters/mon_3_a1.png", COUNT_MONSTER * 2 + 0)
		load_texture_mon(result, "#{base}/monsters/mon_3_a2.png", COUNT_MONSTER * 2 + 1)
		load_texture_mon(result, "#{base}/monsters/mon_3_a3.png", COUNT_MONSTER * 2 + 2)
		load_texture_mon(result, "#{base}/monsters/mon_3_a4.png", COUNT_MONSTER * 2 + 3)
		load_texture_mon(result, "#{base}/monsters/mon_3_b1.png", COUNT_MONSTER * 2 + 4)
		load_texture_mon(result, "#{base}/monsters/mon_3_b2.png", COUNT_MONSTER * 2 + 5)
		load_texture_mon(result, "#{base}/monsters/mon_3_b3.png", COUNT_MONSTER * 2 + 6)
		load_texture_mon(result, "#{base}/monsters/mon_3_b4.png", COUNT_MONSTER * 2 + 7)
		load_texture_mon(result, "#{base}/monsters/mon_3_c1.png", COUNT_MONSTER * 2 + 8)
		load_texture_mon(result, "#{base}/monsters/mon_3_c2.png", COUNT_MONSTER * 2 + 9)
		load_texture_mon(result, "#{base}/monsters/mon_3_c3.png", COUNT_MONSTER * 2 + 10)
		load_texture_mon(result, "#{base}/monsters/mon_3_c4.png", COUNT_MONSTER * 2 + 11)
		load_texture_mon(result, "#{base}/monsters/mon_3_d1.png", COUNT_MONSTER * 2 + 12)
		load_texture_mon(result, "#{base}/monsters/mon_3_d2.png", COUNT_MONSTER * 2 + 13)
		load_texture_mon(result, "#{base}/monsters/mon_3_d3.png", COUNT_MONSTER * 2 + 14)
		load_texture_mon(result, "#{base}/monsters/mon_3_e.png",  COUNT_MONSTER * 2 + 15)

		load_texture_mon(result, "#{base}/monsters/mon_4_a1.png", COUNT_MONSTER * 3 + 0)
		load_texture_mon(result, "#{base}/monsters/mon_4_a2.png", COUNT_MONSTER * 3 + 1)
		load_texture_mon(result, "#{base}/monsters/mon_4_a3.png", COUNT_MONSTER * 3 + 2)
		load_texture_mon(result, "#{base}/monsters/mon_4_a4.png", COUNT_MONSTER * 3 + 3)
		load_texture_mon(result, "#{base}/monsters/mon_4_b1.png", COUNT_MONSTER * 3 + 4)
		load_texture_mon(result, "#{base}/monsters/mon_4_b2.png", COUNT_MONSTER * 3 + 5)
		load_texture_mon(result, "#{base}/monsters/mon_4_b3.png", COUNT_MONSTER * 3 + 6)
		load_texture_mon(result, "#{base}/monsters/mon_4_b4.png", COUNT_MONSTER * 3 + 7)
		load_texture_mon(result, "#{base}/monsters/mon_4_c1.png", COUNT_MONSTER * 3 + 8)
		load_texture_mon(result, "#{base}/monsters/mon_4_c2.png", COUNT_MONSTER * 3 + 9)
		load_texture_mon(result, "#{base}/monsters/mon_4_c3.png", COUNT_MONSTER * 3 + 10)
		load_texture_mon(result, "#{base}/monsters/mon_4_c4.png", COUNT_MONSTER * 3 + 11)
		load_texture_mon(result, "#{base}/monsters/mon_4_d1.png", COUNT_MONSTER * 3 + 12)
		load_texture_mon(result, "#{base}/monsters/mon_4_d2.png", COUNT_MONSTER * 3 + 13)
		load_texture_mon(result, "#{base}/monsters/mon_4_d3.png", COUNT_MONSTER * 3 + 14)
		load_texture_mon(result, "#{base}/monsters/mon_4_e.png",  COUNT_MONSTER * 3 + 15)

		result.write(File.dirname(__FILE__) + '/../../source/res/drawable-nodpi/texmap_mon.png')
	end
end

tmc = TexMapCreator.new

tmc.process
tmc.process_mon
