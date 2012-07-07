task :default do
  Dir.glob('data/*.workout').each do |f|
    sh "adb push #{f} /sdcard/laufwunder/workouts"
  end
end
