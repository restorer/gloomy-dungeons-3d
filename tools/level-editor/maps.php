<?php

$mode = (isset($_REQUEST['mode']) ? $_REQUEST['mode'] : '');

switch (strtolower($mode))
{
	case 'load_map':
		$name = preg_replace('/^[a-z0-9\-_.]/i', '', $_REQUEST['name']);
		echo @file_get_contents("levels/$name");
		break;

	case 'save_map':
		$name = preg_replace('/^[a-z0-9\-_.]/i', '', $_REQUEST['name']);
		$data = $_REQUEST['map'];

		$res = @file_put_contents("levels/$name", $data);

		echo $res ? "File [$name] saved" : "Cant save file [$name]";
		break;

	default:
		$handle = opendir('levels');
		$res = array();

		if ($handle) {
			while (false !== ($file = readdir($handle))) {
				if (!is_dir("levels/$file") && strpos($file, "actions") !== 0) {
					$res[] = $file;
				}
			}
		}

		natsort($res);
		$res = array_values($res);

		echo json_encode($res);
		break;
}
